package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.action.UploadBaseAction;
import com.intellij.openapi.application.ApplicationManager;

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
                thread.setContextClassLoader(UploadBaseAction.class.getClassLoader());

                runWithPluginClassLoader();
            } finally {
                thread.setContextClassLoader(cl);
            }
        }

        public abstract void runWithPluginClassLoader();
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
}
