package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.lang.reflect.Proxy;

/**
 * Covers how the project prism context gets built - never on the caller's thread. The first
 * access answers with the default context and schedules one background build, concurrent first
 * accesses schedule it once, and the environment cache reload event rebuilds it.
 */
public class StudioPrismContextServiceReloadTest extends BasePlatformTestCase {

    public void testFirstAccessReturnsDefaultContextAndBuildsProjectContextInBackground() throws Exception {
        StudioPrismContextService service = new StudioPrismContextService(getProject());

        PrismContext first = service.getPrismContext();
        assertSame("default context while the project context builds",
                StudioPrismContextService.getDefaultPrismContext(), first);

        service.waitForReload();

        PrismContext built = service.getPrismContext();
        assertNotSame("project context after the build", first, built);
        assertSame("stable afterwards", built, service.getPrismContext());
        assertEquals(1, service.getReloadCount());
    }

    public void testConcurrentFirstAccessesScheduleOneBuild() throws Exception {
        StudioPrismContextService service = new StudioPrismContextService(getProject());

        Thread[] threads = new Thread[4];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(service::getPrismContext);
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join(10_000);
        }

        service.waitForReload();

        assertEquals(1, service.getReloadCount());
        assertNotSame(StudioPrismContextService.getDefaultPrismContext(), service.getPrismContext());
    }

    /**
     * PrismContextImpl registers itself through PrismService in its constructor, before it is
     * initialized. Only the build running in this service may accept that, and only for its own
     * thread: publishing the registered instance to readers handed inspections a context whose
     * schema registry state was still null.
     */
    public void testStraySetWithProjectBoundIsIgnoredOutsideBuild() throws Exception {
        StudioPrismContextService service = new StudioPrismContextService(getProject());
        service.getPrismContext();
        service.waitForReload();
        PrismContext built = service.getPrismContext();

        PrismContext stray = (PrismContext) Proxy.newProxyInstance(
                PrismContext.class.getClassLoader(),
                new Class<?>[]{PrismContext.class},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException("stray prism context: " + method.getName());
                });

        StudioPrismContextService.runWithProject(getProject(), () -> service.acceptFromPrismService(stray));

        assertSame("stray set ignored", built, service.getPrismContext());
    }

    public void testEnvironmentCacheReloadRebuildsProjectContext() throws Exception {
        StudioPrismContextService service = new StudioPrismContextService(getProject());

        service.getPrismContext();
        service.waitForReload();
        PrismContext built = service.getPrismContext();

        getProject().getMessageBus().syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC)
                .environmentCacheManagerReloaded();
        service.waitForReload();

        PrismContext rebuilt = service.getPrismContext();
        assertNotSame("rebuilt after the environment caches changed", built, rebuilt);
        assertEquals(2, service.getReloadCount());
    }
}
