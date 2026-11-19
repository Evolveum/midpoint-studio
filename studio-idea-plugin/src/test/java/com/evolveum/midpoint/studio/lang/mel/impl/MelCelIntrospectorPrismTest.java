package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismService;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * MEL introspection runs on annotator and completion threads, where Studio's PrismService
 * override waits for the default prism context and that wait is cancelled under progress.
 * midPoint's CelMidPointExtensions constructor calls PrismContext.get(), so introspection
 * must run with prism suppressed, and a cancellation must not leave the registry empty for
 * the rest of the IDE session.
 */
public class MelCelIntrospectorPrismTest {

    /**
     * Stand in for the Studio override: null while prism is suppressed, otherwise the wait
     * for the default context cancelled.
     */
    private static final PrismService CANCELLING_UNLESS_SUPPRESSED = new PrismService.Mutable() {
        @Override
        public PrismContext prismContext() {
            if (StudioPrismContextService.isPrismContextSuppressed()) {
                return null;
            }
            throw new ProcessCanceledException();
        }
    };

    private static final PrismService ALWAYS_CANCELLING = new PrismService.Mutable() {
        @Override
        public PrismContext prismContext() {
            throw new ProcessCanceledException();
        }
    };

    private static <T> T withPrismService(PrismService service, Supplier<T> action) {
        // loads StudioPrismContextService first, its static initializer installs the Studio
        // override and would replace the fake if it ran later
        StudioPrismContextService.isPrismContextSuppressed();

        PrismService previous = PrismService.get();
        PrismService.overrideSupplier(() -> service);
        try {
            return action.get();
        } finally {
            PrismService.overrideSupplier(() -> previous);
        }
    }

    @Test
    public void introspectionRunsWithPrismSuppressed() {
        MelExtensionRegistry registry = withPrismService(CANCELLING_UNLESS_SUPPRESSED, MelExtensionRegistry::create);

        assertTrue("registry empty after cancelled PrismContext.get() during introspection",
                registry.isNamespace("format"));
    }

    @Test
    public void cancellationIsRethrownAndNotCached() {
        MelExtensionRegistry.reset();
        try {
            assertThrows(ProcessCanceledException.class,
                    () -> withPrismService(ALWAYS_CANCELLING, MelExtensionRegistry::get));

            MelExtensionRegistry registry = withPrismService(CANCELLING_UNLESS_SUPPRESSED, MelExtensionRegistry::get);
            assertTrue("registry stayed empty after a cancelled first attempt", registry.isNamespace("format"));
        } finally {
            MelExtensionRegistry.reset();
        }
    }
}
