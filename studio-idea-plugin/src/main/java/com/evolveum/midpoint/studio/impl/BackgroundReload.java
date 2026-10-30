package com.evolveum.midpoint.studio.impl;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.hints.ParameterHintsPassFactory;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs a piece of work in the background, one run at a time. Requests made while a run is in
 * progress coalesce into exactly one more run after it, so a request is never lost and never
 * duplicated. Nothing waits on the caller's thread, the caller keeps what it has until the run
 * lands.
 *
 * Runs on the application pool, as a background task with progress when a title is given, or on
 * a plain daemon thread when there is no Application (plain unit tests).
 */
public final class BackgroundReload {

    private static final Logger LOG = Logger.getInstance(BackgroundReload.class);

    private final String name;

    private final Runnable work;

    private final String progressTitle;

    private final AtomicBoolean requested = new AtomicBoolean();

    private final AtomicBoolean running = new AtomicBoolean();

    private volatile Future<?> current;

    public BackgroundReload(@NotNull String name, @NotNull Runnable work) {
        this(name, work, null);
    }

    private BackgroundReload(String name, Runnable work, String progressTitle) {
        this.name = name;
        this.work = work;
        this.progressTitle = progressTitle;
    }

    /**
     * Same work, shown as a background task with this title in the status bar.
     */
    public @NotNull BackgroundReload withProgressTitle(@NotNull String progressTitle) {
        return new BackgroundReload(name, work, progressTitle);
    }

    /**
     * Project bound work: the project key is bound around it so prism resolves definitions in the
     * project context, highlighting is restarted after each run, nothing runs for a disposed project.
     */
    public static @NotNull BackgroundReload forProject(@NotNull Project project, @NotNull String name, @NotNull Runnable work) {
        return new BackgroundReload(name, () -> {
            if (project.isDisposed()) {
                return;
            }

            StudioPrismContextService.runWithProject(project, work);
            restartHighlighting(project);
        });
    }

    public void request() {
        requested.set(true);
        if (!running.compareAndSet(false, true)) {
            return;
        }

        current = execute(this::runLoop);
    }

    public boolean isRunning() {
        return running.get();
    }

    /**
     * Waits until no run is in progress or pending. For tests.
     */
    public void waitForIdle(@NotNull Duration timeout) throws InterruptedException, TimeoutException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            Future<?> future = current;
            if (future != null) {
                long left = deadline - System.nanoTime();
                if (left <= 0) {
                    throw new TimeoutException("Background reload of " + name + " still running");
                }
                try {
                    future.get(left, TimeUnit.NANOSECONDS);
                } catch (ExecutionException ignored) {
                    // the loop logs failures itself
                }
            }

            if (!running.get() && !requested.get()) {
                return;
            }

            if (System.nanoTime() >= deadline) {
                throw new TimeoutException("Background reload of " + name + " still pending");
            }
            // a follow-up run is being scheduled, give it a moment
            Thread.sleep(5);
        }
    }

    private void runLoop() {
        try {
            while (requested.getAndSet(false)) {
                long start = System.currentTimeMillis();
                LOG.info("Background reload of " + name + " started");
                try {
                    work.run();
                    LOG.info("Background reload of " + name + " finished in " + (System.currentTimeMillis() - start) + " ms");
                } catch (Throwable ex) {
                    if (ex instanceof ControlFlowException) {
                        LOG.debug("Background reload of " + name + " cancelled", ex);
                    } else {
                        LOG.warn("Background reload of " + name + " failed", ex);
                    }
                }
            }
        } finally {
            running.set(false);
        }

        // a request that arrived after the loop ended but before the flag was released
        if (requested.get()) {
            request();
        }
    }

    private Future<?> execute(Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        if (application == null) {
            FutureTask<Void> task = new FutureTask<>(runnable, null);
            Thread thread = new Thread(task, name);
            thread.setDaemon(true);
            thread.start();
            return task;
        }

        if (progressTitle == null) {
            return AppExecutorUtil.getAppExecutorService().submit(runnable);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        ProgressManager.getInstance().run(new Task.Backgroundable(null, progressTitle, false) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    runnable.run();
                    future.complete(null);
                } catch (Throwable ex) {
                    future.completeExceptionally(ex);
                }
            }
        });
        return future;
    }

    private static void restartHighlighting(@NotNull Project project) {
        Application application = ApplicationManager.getApplication();
        if (application == null || application.isUnitTestMode()) {
            return;
        }

        application.invokeLater(() -> {
            ParameterHintsPassFactory.forceHintsUpdateOnNextPass();
            DaemonCodeAnalyzer.getInstance(project).restart();
        }, project.getDisposed());
    }
}
