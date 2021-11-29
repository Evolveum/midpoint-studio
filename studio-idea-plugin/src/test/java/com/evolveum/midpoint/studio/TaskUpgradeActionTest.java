package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.studio.action.TaskUpgradeAction;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.xalan.xslt.EnvironmentCheck;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.File;
import java.io.PrintWriter;

public class TaskUpgradeActionTest extends StudioActionTest {

    private static final Logger LOG = Logger.getInstance(TaskUpgradeActionTest.class);

    @BeforeAll
    protected void beforeAll() throws Exception {
        super.beforeAll();

        EnvironmentCheck check = new EnvironmentCheck();
        check.checkEnvironment(new PrintWriter(System.out));
    }

    @Override
    protected String getTestFolder() {
        return "task-upgrade";
    }

    @Test
    public void testImport1Multinode() {
        testUpgrade("import-1-input.xml", "import-1-output.xml");
    }

    @Test
    public void testLivesync1Simple() {
        testUpgrade("ls-1-input.xml", "ls-1-output.xml");
    }

    @Test
    public void testLivesync2ErrorHandlingStrategy() {
        testUpgrade("ls-2-input.xml", "ls-2-output.xml");
    }

    @Test
    public void testAlreadyProcessed() {
        testUpgrade("ls-1-output.xml", "ls-1-output.xml");
    }

    @Test
    public void testReconcilation1Simple() {
        testUpgrade("recon-1-input.xml", "recon-1-output.xml");
    }

    @Test
    public void testReconcilation2Multinode() {
        testUpgrade("recon-2-input.xml", "recon-2-output.xml");
    }

    @Test
    public void testRecomputation1Simple() {
        testUpgrade("recomputation-1-input.xml", "recomputation-1-output.xml");
    }

    @Test
    public void testRecomputation2Multinode() {
        testUpgrade("recomputation-2-input.xml", "recomputation-2-output.xml");
    }

    @Test
    public void testImport2NoArchetypeMultinode() {
        testUpgrade("import-2-input.xml", "import-2-output.xml");
    }

    @Test
    public void testImport3WithQuery() {
        testUpgrade("import-3-input.xml", "import-3-output.xml");
    }

    @Test
    public void testBulkIterative1Multinode() {
        testUpgrade("bulk-iterative-1-input.xml", "bulk-iterative-1-output.xml");
    }

    @Test
    public void testBulkIterative2() {
        testUpgrade("bulk-iterative-2-input.xml", "bulk-iterative-2-output.xml");
    }

    @Test
    public void testMultiple() {
        testUpgrade("multiple-input.xml", "multiple-output.xml");
    }

    @Test
    public void testAsync1() {
        testUpgrade("async-update-1-input.xml", "async-update-1-output.xml");
    }

    @Test
    public void testShadowRefresh() {
        testUpgrade("shadow-refresh-1-input.xml", "shadow-refresh-1-output.xml");
    }

    @Test
    public void testShadowIntegrity() {
        testUpgrade("shadow-integrity-1-input.xml", "shadow-integrity-1-output.xml");
    }

    @Test
    public void testScripting1() {
        testUpgrade("scripting-1-input.xml", "scripting-1-output.xml");
    }

    @Test
    public void testReindex1() {
        testUpgrade("reindex-1-input.xml", "reindex-1-output.xml");
    }

    @Test
    public void testReindex2() {
        testUpgrade("reindex-2-input.xml", "reindex-2-output.xml");
    }

    @Test
    public void testDelete1() {
        testUpgrade("delete-1-input.xml", "delete-1-output.xml");
    }

    @Test
    public void testDelete2() {
        testUpgrade("delete-2-input.xml", "delete-2-output.xml");
    }

    @Test
    public void testUnknownAction() {
        testUpgrade("unknown-1-input.xml", "unknown-1-input.xml");
    }

    private void testUpgrade(String input, String validation) {
        LOG.info("Testing upgrade for " + input + ", validating using " + validation);

        myFixture.configureByFiles(input);
        myFixture.testAction(new TaskUpgradeAction());

        String text = myFixture.getEditor().getDocument().getText();

        LOG.info("Created task:\n" + text);

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
