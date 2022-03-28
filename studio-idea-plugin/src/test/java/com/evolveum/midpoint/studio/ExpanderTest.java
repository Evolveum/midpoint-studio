package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.studio.impl.EncryptionService;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.Expander;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.File;

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

        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
        Expander expander = new Expander(environment, cm, project);

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
