package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.ModuleManagerEx;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

/**
 * Created by Viliam Repan (lazyman).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class StudioActionTest extends LightJavaCodeInsightFixtureTestCase {

    public static final String TEST_DATA_PATH = "./src/test/testData/";

    @BeforeAll
    protected void beforeAll() throws Exception {
        super.setUp();

        WriteAction.runAndWait(() -> {
            ModuleManagerEx moduleManager = ModuleManagerEx.getInstanceEx(getProject());
            Module module = moduleManager.getModules()[0];
            FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
            FacetManager.getInstance(module).addFacet(facetType, facetType.getDefaultFacetName(), null);
        });
    }

    @AfterAll
    protected void afterAll() throws Exception {
        super.tearDown();
    }

    @Override
    protected String getTestDataPath() {
        return TEST_DATA_PATH + getTestFolder();
    }

    protected abstract String getTestFolder();
}
