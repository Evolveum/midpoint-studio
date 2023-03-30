package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.Expander;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExpanderTest extends StudioActionTest {

    @Override
    protected String getTestFolder() {
        return "expander";
    }

    @Test()
    public void testExpandFromFileNonexistingChunk() {
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, () -> {
            testExpandFile(null, "system-configuration.xml");
        });

        assertEquals("Can't load content for key '@./insert2.txt', file './insert2.txt' is not present in 'temp:///src'", thrown.getMessage());
    }

    @Test()
    public void testExpandFromFileExistingChunk() {
        testExpandFile("system-configuration-expected.xml", "system-configuration.xml", "insert2.txt");
    }

    @Test
    public void testMid7781() {
        testExpandFile("mid-7781/expected.xml", "mid-7781/functionalLibraries/lib.xml", "mid-7781/functionalLibraries/lib.xml", "mid-7781/include/email/sample.html");
    }

    @Test
    public void testMid7781Variant() throws IOException {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            testMid7781Variants("$(@..\\include\\email\\sample.html)");
        }

        testMid7781Variants("$(@../include/email/sample.html)");
    }

    private void testMid7781Variants(String keyToExpand) throws IOException {
        Project project = getProject();
        EnvironmentService es = EnvironmentService.getInstance(project);
        MidPointService ms = MidPointService.getInstance(project);
        Expander expander = new Expander(es.getSelected(), null, project, ms.getSettings().isIgnoreMissingKeys());

        PsiFile psiFile = myFixture.configureByFile("mid-7781/functionalLibraries/lib.xml");
        VirtualFile file = psiFile.getVirtualFile();

        myFixture.configureByFile("mid-7781/include/email/sample.html");

        String expected = FileUtils.readFileToString(new File("./src/test/testData/expander/mid-7781/include/email/sample.html"), StandardCharsets.UTF_8);

        String value = expander.expand(keyToExpand, file);
        assertEquals(expected, value);
    }

    private void testExpandFile(String fileExpected, String fileToExpand, String... expandChunkFiles) {
        for (String s : expandChunkFiles) {
            myFixture.configureByFile(s);
        }

        PsiFile file = myFixture.configureByFile(fileToExpand);

        Editor editor = myFixture.getEditor();
        String text = editor.getDocument().getText();

        Project project = getProject();

        Environment environment = new Environment();

        EnvironmentService es = EnvironmentService.getInstance(project);
        es.add(environment);

        Expander expander = new Expander(environment, project);

        String result = expander.expand(text, file.getVirtualFile());

        LOG.info("Expanded result:\n" + result);

        Diff d = DiffBuilder
                .compare(Input.fromFile(new File(getTestDataPath(), fileExpected)))
                .withTest(Input.fromString(result))
                .build();

        if (d.hasDifferences()) {
            LOG.error(d.fullDescription());
            fail();
        }
    }
}
