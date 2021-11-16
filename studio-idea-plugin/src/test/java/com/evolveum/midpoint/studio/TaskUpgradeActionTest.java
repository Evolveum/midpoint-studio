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
    public void testImport1() {
        myFixture.configureByFiles("import-1-input.xml");
        myFixture.testAction(new TaskUpgradeAction());

        String text = myFixture.getEditor().getDocument().getText();

        Diff d = DiffBuilder
                .compare(Input.fromFile(new File(getTestDataPath(), "import-1-output.xml")))
                .withTest(Input.fromString(text))
                .build();

        if (d.hasDifferences()) {
            LOG.info(d.fullDescription());
        }

        assertFalse(d.hasDifferences());
    }
}
