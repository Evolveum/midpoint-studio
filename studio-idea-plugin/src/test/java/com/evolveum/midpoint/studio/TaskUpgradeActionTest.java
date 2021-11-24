package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.studio.action.TaskUpgradeAction;
import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.ModuleManagerEx;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.apache.xalan.xslt.EnvironmentCheck;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by Viliam Repan (lazyman).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskUpgradeActionTest extends LightJavaCodeInsightFixtureTestCase {

    public static final String TEST_DATA_PATH = "./src/test/testData";

    private static final Logger LOG = Logger.getInstance(TaskUpgradeActionTest.class);

    @BeforeAll
    protected void beforeAll() throws Exception {
        super.setUp();

        WriteAction.runAndWait(() -> {
            ModuleManagerEx moduleManager = ModuleManagerEx.getInstanceEx(getProject());
            Module module = moduleManager.getModules()[0];
            FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
            FacetManager.getInstance(module).addFacet(facetType, facetType.getDefaultFacetName(), null);
        });

        EnvironmentCheck check = new EnvironmentCheck();
        check.checkEnvironment(new PrintWriter(System.out));
    }

    @AfterAll
    protected void afterAll() throws Exception {
        super.tearDown();
    }

    @Override
    protected String getTestDataPath() {
        return TEST_DATA_PATH + "/task-upgrade";
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
    public void testBulkIterative1Multinode() {
        testUpgrade("bulk-iterative-1-input.xml", "bulk-iterative-1-output.xml");
    }

    @Test
    public void testMultiple() {
        testUpgrade("multiple-input.xml", "multiple-output.xml");
    }

    @Test
    public void testAsync1() {
        testUpgrade("async-update-1-input.xml", "async-update-1-output.xml");
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
