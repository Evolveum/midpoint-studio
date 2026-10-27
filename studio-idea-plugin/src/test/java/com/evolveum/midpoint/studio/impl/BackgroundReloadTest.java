package com.evolveum.midpoint.studio.impl;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Covers backround reload, single execution without a platform.
 */
public class BackgroundReloadTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    public void testRequestsDuringRunCoalesceIntoOneMoreRun() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger runs = new AtomicInteger();
        BackgroundReload reload = new BackgroundReload("test", () -> {
            if (runs.incrementAndGet() == 1) {
                started.countDown();
                try {
                    assertTrue(release.await(10, TimeUnit.SECONDS));
                } catch (InterruptedException ex) {
                    throw new AssertionError(ex);
                }
            }
        });

        reload.request();
        assertTrue(started.await(10, TimeUnit.SECONDS));
        assertTrue("running while the first run is blocked", reload.isRunning());

        for (int i = 0; i < 5; i++) {
            reload.request();
        }

        release.countDown();
        reload.waitForIdle(TIMEOUT);

        assertEquals("first run plus exactly one follow-up", 2, runs.get());
        assertFalse(reload.isRunning());
    }

    @Test
    public void testIdleRequestRunsOnce() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        BackgroundReload reload = new BackgroundReload("test", runs::incrementAndGet);

        reload.request();
        reload.waitForIdle(TIMEOUT);

        assertEquals(1, runs.get());
    }

    @Test
    public void testFailingRunDoesNotBlockLaterRequests() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        BackgroundReload reload = new BackgroundReload("test", () -> {
            if (runs.incrementAndGet() == 1) {
                throw new IllegalStateException("first run fails");
            }
        });

        reload.request();
        reload.waitForIdle(TIMEOUT);
        reload.request();
        reload.waitForIdle(TIMEOUT);

        assertEquals(2, runs.get());
        assertFalse(reload.isRunning());
    }

    @Test
    public void testWaitForIdleWithoutRequestReturnsImmediately() throws Exception {
        BackgroundReload reload = new BackgroundReload("test", () -> fail("must not run"));

        reload.waitForIdle(TIMEOUT);

        assertFalse(reload.isRunning());
    }
}
