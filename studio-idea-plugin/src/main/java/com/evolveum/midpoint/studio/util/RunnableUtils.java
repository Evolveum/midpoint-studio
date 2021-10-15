package com.evolveum.midpoint.studio.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import org.jetbrains.concurrency.CancellablePromise;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class RunnableUtils {

    public static abstract class PluginClasspathRunnable implements Runnable {

        @Override
        public void run() {
            Thread thread = Thread.currentThread();

            ClassLoader cl = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader(RunnableUtils.class.getClassLoader());

                runWithPluginClassLoader();
            } finally {
                thread.setContextClassLoader(cl);
            }
        }

        public abstract void runWithPluginClassLoader();
    }

    public static abstract class PluginClassCallable<R> implements Callable<R> {

        @Override
        public R call() throws Exception {
            Thread thread = Thread.currentThread();

            ClassLoader cl = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader(RunnableUtils.class.getClassLoader());

                return callWithPluginClassLoader();
            } finally {
                thread.setContextClassLoader(cl);
            }
        }

        public abstract R callWithPluginClassLoader() throws Exception;
    }

    public static <T> T executeWithPluginClassloader(Supplier<T> supplier) {
        PluginClassCallable<T> c = new PluginClassCallable<>() {

            @Override
            public T callWithPluginClassLoader() throws Exception {
                return supplier.get();
            }
        };

        try {
            return c.call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void runReadAction(Runnable runnable) {
        ApplicationManager.getApplication().runReadAction(
                new PluginClasspathRunnable() {

                    @Override
                    public void runWithPluginClassLoader() {
                        runnable.run();
                    }
                }
        );
    }

    public static void runWriteAction(Runnable runnable) {
        ApplicationManager.getApplication().runWriteAction(
                new PluginClasspathRunnable() {

                    @Override
                    public void runWithPluginClassLoader() {
                        runnable.run();
                    }
                }
        );
    }

    public static void runWriteActionAndWait(Runnable runnable) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            runWriteAction(runnable);
        });
    }

    public static CancellablePromise<Void> submitNonBlockingReadAction(Runnable runnable, ExecutorService executor) {
        return ReadAction.nonBlocking(new PluginClasspathRunnable() {

            @Override
            public void runWithPluginClassLoader() {
                runnable.run();
            }
        }).submit(executor);
    }
}
