package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismService;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Covers nesting of run*WithProject wrappers with a real project. A nested wrapper (or a prism
 * context reload triggered from the supplier) must restore the key of the outer wrapper instead of
 * removing it. Otherwise, the outer context is lost for the rest of the wrapper's execution and
 * every subsequent PrismContext.get() call silently falls back to the default prism context.
 *
 * The test project has no environment selected, so the project prism context is built with
 * zero extension schemas. The assertions are on routing (project context vs. default context)
 * and on the fallback counter, not on the context content.
 */
public class StudioPrismContextServiceNestingTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        StudioPrismContextService.resetDefaultContextFallbackCount();
    }

    public void testOuterContextKeySurvivesNestedPrismContextReload() {
        Project project = getProject();

        StudioPrismContextService.runWithProject(project, () -> {
            // first access: the service context may be null, so the supplier answers with the
            // default context and schedules the project context build in the background
            PrismService.get().prismContext();
            try {
                StudioPrismContextService.get(project).waitForReload();
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }

            // the outer runWithProject key must still be bound, so this call is routed to the
            // project context and must not fall back to the default prism context
            PrismContext context = PrismService.get().prismContext();

            assertSame(StudioPrismContextService.get(project).getPrismContext(), context);
            assertNotSame(StudioPrismContextService.getDefaultPrismContext(), context);
            assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
        });
    }

    /**
     * Shape of the InitialObjectsCache reload: createReloadQuery runs runCallableWithProject
     * inside the runWithProject of EnvironmentCacheManager, then the objects are parsed.
     */
    public void testNestedCallableWithProjectRestoresOuterProjectKey() throws Exception {
        Project project = getProject();

        StudioPrismContextService.runWithProject(project, () -> {
            try {
                StudioPrismContextService.runCallableWithProject(project, () -> null);
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }

            PrismContext context = PrismService.get().prismContext();

            assertSame(StudioPrismContextService.get(project).getPrismContext(), context);
            assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
        });
    }

    public void testNestedDefaultContextWrapperRestoresOuterProjectKey() {
        Project project = getProject();

        StudioPrismContextService.runWithProject(project, () -> {
            try {
                StudioPrismContextService.runCallableWithDefaultContext(() -> null);
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }

            PrismContext context = PrismService.get().prismContext();

            assertSame(StudioPrismContextService.get(project).getPrismContext(), context);
            assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
        });
    }

    public void testThreadLocalIsEmptyAfterOutermostProjectWrapper() {
        Project project = getProject();

        StudioPrismContextService.runWithProject(project, () -> PrismService.get().prismContext());

        // no key is bound anymore, so this call counts as a fallback to the default context
        PrismContext context = PrismService.get().prismContext();

        assertSame(StudioPrismContextService.getDefaultPrismContext(), context);
        assertEquals(1, StudioPrismContextService.getDefaultContextFallbackCount());
    }
}
