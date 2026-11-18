package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Base for the platform tests of this plugin. Waits in tearDown for the project prism context
 * build a test may have started, so the build and the services it creates do not leak into tests
 * that run later in the same JVM. Also carries the helpers tests share.
 */
public abstract class StudioPlatformTestCase extends BasePlatformTestCase {

    @Override
    protected void tearDown() throws Exception {
        try {
            awaitPrismContextBuild();
        } finally {
            super.tearDown();
        }
    }

    protected void awaitPrismContextBuild() {
        if (myFixture == null || getProject().isDisposed()) {
            return;
        }

        StudioPrismContextService service = getProject().getServiceIfCreated(StudioPrismContextService.class);
        if (service != null) {
            service.awaitReload(new EmptyProgressIndicator());
        }
    }

    protected void addMidPointFacet() {
        FacetType<?, ?> facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        WriteAction.run(() -> FacetUtil.addFacet(getModule(), facetType, MidPointFacetType.FACET_NAME));
    }
}
