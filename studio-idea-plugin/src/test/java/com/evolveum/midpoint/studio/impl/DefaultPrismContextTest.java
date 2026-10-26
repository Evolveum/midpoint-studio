package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Covers the default prism context holder without a running platform and without building a
 * real prism context.
 */
public class DefaultPrismContextTest {

    private static PrismContext inert() {
        return (PrismContext) Proxy.newProxyInstance(
                PrismContext.class.getClassLoader(),
                new Class<?>[]{PrismContext.class},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException("Inert prism context: " + method.getName());
                });
    }

    @Test
    public void testGetIfReadyDoesNotWaitAndGetDoes() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        PrismContext context = inert();
        DefaultPrismContext holder = new DefaultPrismContext(() -> {
            try {
                assertTrue("builder released", release.await(10, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw new AssertionError(ex);
            }
            return context;
        });

        assertNull("nothing built before start", holder.getIfReady());

        holder.ensureStarted();
        assertNull("still building", holder.getIfReady());

        release.countDown();
        assertSame(context, holder.get());
        assertSame(context, holder.getIfReady());
    }

    @Test
    public void testBuilderRunsOnceForConcurrentCallers() throws Exception {
        AtomicInteger builds = new AtomicInteger();
        PrismContext context = inert();
        DefaultPrismContext holder = new DefaultPrismContext(() -> {
            builds.incrementAndGet();
            return context;
        });

        PrismContext[] results = new PrismContext[4];
        Thread[] threads = new Thread[results.length];
        for (int i = 0; i < threads.length; i++) {
            int idx = i;
            threads[i] = new Thread(() -> results[idx] = holder.get());
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join(10_000);
        }

        assertEquals(1, builds.get());
        for (PrismContext result : results) {
            assertSame(context, result);
        }
    }

    /**
     * Shape of a prism context build: the PrismContextImpl constructor registers itself through
     * PrismService, then initialize() asks PrismContext.get() for that very instance. On the
     * building thread the holder must hand out the instance under construction instead of waiting
     * for its own future.
     */
    @Test(timeout = 10_000)
    public void testBuildingThreadSeesContextUnderConstructionInsteadOfWaiting() {
        PrismContext context = inert();
        Object[] seen = new Object[3];
        DefaultPrismContext[] holder = new DefaultPrismContext[1];
        holder[0] = new DefaultPrismContext(() -> {
            seen[0] = holder[0].getUnlessBuilding();
            seen[1] = holder[0].acceptUnderConstruction(context);
            seen[2] = holder[0].getUnlessBuilding();
            return context;
        });

        assertFalse("not building on this thread", holder[0].acceptUnderConstruction(inert()));

        assertSame(context, holder[0].get());
        assertNull("nothing registered yet", seen[0]);
        assertEquals("registration accepted on the building thread", Boolean.TRUE, seen[1]);
        assertSame("instance under construction", context, seen[2]);
        assertSame("after the build the same call waits and returns the context", context, holder[0].getUnlessBuilding());
    }

    @Test
    public void testFailedBuildIsRetriedByNextGet() {
        AtomicInteger builds = new AtomicInteger();
        PrismContext context = inert();
        DefaultPrismContext holder = new DefaultPrismContext(() -> {
            if (builds.incrementAndGet() == 1) {
                throw new IllegalStateException("schema broken");
            }
            return context;
        });

        try {
            holder.get();
            fail("first get must report the failed build");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("schema broken"));
        }
        assertNull("nothing usable after a failed build", holder.getIfReady());

        assertSame(context, holder.get());
        assertEquals(2, builds.get());
    }
}
