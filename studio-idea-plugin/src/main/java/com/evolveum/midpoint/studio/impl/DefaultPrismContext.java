package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.hints.ParameterHintsPassFactory;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Holds the default prism context (no project, no extension schemas). The context is built once,
 * lazily, in the background: a static initializer would run on whatever thread touches the class
 * first and park every other thread on the class init lock without cancellation or progress.
 * A failed build is reported to the caller and retried by the next access.
 */
public final class DefaultPrismContext {

    private static final Logger LOG = Logger.getInstance(DefaultPrismContext.class);

    private static final DefaultPrismContext INSTANCE =
            new DefaultPrismContext(StudioPrismContextService::createDefaultPrismContext);

    private final Supplier<PrismContext> builder;

    private final BackgroundReload build =
            new BackgroundReload("default prism context", this::buildOnce)
                    .withProgressTitle("Loading midPoint schemas");

    private final AtomicBoolean edtWaitReported = new AtomicBoolean();

    private volatile CompletableFuture<PrismContext> future;

    private volatile Thread buildingThread;

    private volatile PrismContext underConstruction;

    @VisibleForTesting
    DefaultPrismContext(@NotNull Supplier<PrismContext> builder) {
        this.builder = builder;
    }

    public static @NotNull DefaultPrismContext getInstance() {
        return INSTANCE;
    }

    /**
     * Starts the build unless it is running or done. Cheap, safe from any thread. The monitor
     * only guards the swap of the future, the build runs outside of it: the platform may run the
     * task on another thread while the caller waits, and that thread calls back into this holder.
     */
    public void ensureStarted() {
        synchronized (this) {
            CompletableFuture<PrismContext> current = future;
            if (current != null && !current.isCompletedExceptionally()) {
                return;
            }

            future = new CompletableFuture<>();
        }

        build.request();
    }

    /**
     * Waits for the context. Cancellable under a progress indicator or a cancellable read action,
     * a plain wait otherwise. Callers on the EDT are reported once, they should run in the background.
     */
    public @NotNull PrismContext get() {
        CompletableFuture<PrismContext> current = future;
        if (current == null || current.isCompletedExceptionally()) {
            ensureStarted();
            current = future;
        }

        if (!current.isDone()) {
            reportWaitOnEdt();
        }

        boolean checkCanceled = ApplicationManager.getApplication() != null;
        while (true) {
            if (checkCanceled) {
                ProgressManager.checkCanceled();
            }

            try {
                return current.get(50, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ignored) {
                // check cancellation and wait again
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof RuntimeException re) {
                    throw re;
                }
                throw new IllegalStateException(cause);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for the default prism context", ex);
            }
        }
    }

    /**
     * For the PrismService supplier: prism internals call PrismContext.get() while the default
     * context itself initializes. On the building thread this returns the instance under
     * construction (null before the constructor registered it), on any other thread it waits
     * like {@link #get()}.
     */
    public @Nullable PrismContext getUnlessBuilding() {
        if (Thread.currentThread() == buildingThread) {
            return underConstruction;
        }

        return get();
    }

    /**
     * For the PrismService setter: the PrismContextImpl constructor registers itself. Accepted only
     * on the building thread, returns whether it was.
     */
    public boolean acceptUnderConstruction(@NotNull PrismContext context) {
        if (Thread.currentThread() != buildingThread) {
            return false;
        }

        underConstruction = context;
        return true;
    }

    /**
     * The context if already built, null otherwise. Never waits, for action updates and painting.
     */
    public @Nullable PrismContext getIfReady() {
        CompletableFuture<PrismContext> current = future;
        if (current == null || !current.isDone() || current.isCompletedExceptionally()) {
            return null;
        }

        return current.getNow(null);
    }

    private void buildOnce() {
        CompletableFuture<PrismContext> result = future;

        long start = System.currentTimeMillis();
        LOG.info("Creating default prism context");
        buildingThread = Thread.currentThread();
        try {
            PrismContext context = builder.get();
            LOG.info("Default prism context created in " + (System.currentTimeMillis() - start) + " ms");

            result.complete(context);
            restartHighlighting();
        } catch (Throwable ex) {
            LOG.warn("Couldn't create default prism context, the next access retries", ex);

            result.completeExceptionally(ex);
        } finally {
            underConstruction = null;
            buildingThread = null;
        }
    }

    private void reportWaitOnEdt() {
        Application application = ApplicationManager.getApplication();
        if (application == null || !application.isDispatchThread() || application.isUnitTestMode()) {
            return;
        }

        if (edtWaitReported.compareAndSet(false, true)) {
            LOG.warn("Waiting for the default prism context on the EDT, the caller should run in the background:",
                    new Exception());
        }
    }

    /**
     * Highlighting that ran before the context was ready could not use it, rerun it.
     */
    private static void restartHighlighting() {
        Application application = ApplicationManager.getApplication();
        if (application == null || application.isUnitTestMode()) {
            return;
        }

        application.invokeLater(() -> {
            for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                if (project.isDisposed()) {
                    continue;
                }

                ParameterHintsPassFactory.forceHintsUpdateOnNextPass();
                DaemonCodeAnalyzer.getInstance(project).restart();
            }
        });
    }
}
