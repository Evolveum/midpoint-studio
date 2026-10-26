package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismService;
import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Covers the PrismService override supplier installed by StudioPrismContextService without
 * a running platform: fallback to the default context when no project is bound to the current
 * thread, flood protection that reports a warning only when the fallback happens too often
 * (usually a missing run*WithProject wrapper), and restoring of the outer context key when
 * run*WithProject wrappers are nested.
 *
 * <p>Project bound wrappers are exercised with a project proxy that fails on any call, since
 * a wrapper that only binds and unbinds the key must not touch the project at all.
 */
public class StudioPrismContextServiceTest {

    @Before
    public void resetFallbackCount() {
        StudioPrismContextService.resetDefaultContextFallbackCount();
    }

    @Test
    public void testReturnsDefaultContextWhenNoProjectBound() {
        PrismContext context = PrismService.get().prismContext();

        assertSame(StudioPrismContextService.getDefaultPrismContext(), context);
        assertEquals(1, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    @Test
    public void testFallbackNotReportedWhenDefaultContextExplicitlyRequested() throws Exception {
        PrismContext context = StudioPrismContextService.runCallableWithDefaultContext(
                () -> PrismService.get().prismContext());

        assertSame(StudioPrismContextService.getDefaultPrismContext(), context);
        assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    @Test
    public void testThreadLocalIsRemovedAfterRunWithDefaultContext() throws Exception {
        StudioPrismContextService.runCallableWithDefaultContext(
                () -> PrismService.get().prismContext());

        // thread local must be empty again, so this call counts as a fallback
        PrismService.get().prismContext();

        assertEquals(1, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    @Test
    public void testContextSetIsIgnoredWhenNoProjectBound() {
        PrismService.get().prismContext(StudioPrismContextService.getDefaultPrismContext());

        PrismContext context = PrismService.get().prismContext();

        assertSame(StudioPrismContextService.getDefaultPrismContext(), context);
        assertEquals(1, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    @Test
    public void testFallbackReportedAtIntervalResetsCounter() {
        long interval = StudioPrismContextService.FALLBACK_LOG_INTERVAL;

        for (long i = 0; i < interval - 1; i++) {
            StudioPrismContextService.reportDefaultContextFallback();
        }
        assertEquals(interval - 1, StudioPrismContextService.getDefaultContextFallbackCount());

        StudioPrismContextService.reportDefaultContextFallback();
        assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    @Test
    public void testNestedRunWithProjectRestoresOuterDefaultKey() throws Exception {
        StudioPrismContextService.runCallableWithDefaultContext(() -> {
            StudioPrismContextService.runWithProject(untouchableProject(), () -> {
            });

            // outer key asks for the default context explicitly, so no fallback is counted
            return PrismService.get().prismContext();
        });

        assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    @Test
    public void testNestedRunSupplierWithProjectRestoresOuterDefaultKey() throws Exception {
        StudioPrismContextService.runCallableWithDefaultContext(() -> {
            StudioPrismContextService.runSupplierWithProject(untouchableProject(), () -> null);

            return PrismService.get().prismContext();
        });

        assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    @Test
    public void testNestedRunCallableWithProjectRestoresOuterDefaultKey() throws Exception {
        StudioPrismContextService.runCallableWithDefaultContext(() -> {
            StudioPrismContextService.runCallableWithProject(untouchableProject(), () -> null);

            return PrismService.get().prismContext();
        });

        assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    @Test
    public void testOuterKeyRestoredWhenNestedWrapperThrows() throws Exception {
        StudioPrismContextService.runCallableWithDefaultContext(() -> {
            try {
                StudioPrismContextService.runWithProject(untouchableProject(), () -> {
                    throw new IllegalStateException("expected");
                });
            } catch (IllegalStateException ex) {
                // expected
            }

            return PrismService.get().prismContext();
        });

        assertEquals(0, StudioPrismContextService.getDefaultContextFallbackCount());
    }

    private static Project untouchableProject() {
        return (Project) Proxy.newProxyInstance(
                Project.class.getClassLoader(),
                new Class<?>[] { Project.class },
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException(
                            "Project must not be touched by a wrapper: " + method.getName());
                });
    }
}
