package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.studio.action.CleanupFileAction;
import com.intellij.openapi.diagnostic.Logger;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.File;

public class CleanupFileActionTest extends StudioActionTest {

    private static final Logger LOG = Logger.getInstance(CleanupFileActionTest.class);

    @Override
    protected String getTestFolder() {
        return "cleanup-file";
    }

    @Test
    public void testCleanup1() {
        testUpgrade("cleanup-1-input.xml", "cleanup-1-output.xml");
    }

    @Test
    public void testCleanup2() {
        testUpgrade("cleanup-2-input.xml", "cleanup-2-output.xml");
    }

    @Test
    public void testCleanup3() {
        testUpgrade("cleanup-3-input.xml", "cleanup-3-output.xml");
    }

    private void testUpgrade(String input, String validation) {
        LOG.info("Testing cleanup file for " + input + ", validating using " + validation);

        myFixture.configureByFiles(input);
        myFixture.testAction(new CleanupFileAction());

        String text = myFixture.getEditor().getDocument().getText();

        LOG.info("Cleaned up content:\n" + text);

        Diff d = DiffBuilder
                .compare(Input.fromFile(new File(getTestDataPath(), validation)))
                .withTest(Input.fromString(text))
                .build();

        if (d.hasDifferences()) {
            LOG.info(d.fullDescription());
            fail();
        }
    }
}
