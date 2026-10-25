package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.client.TestConnectionResult;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.ShowExceptionNotificationAction;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.ui.StudioAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestConnectionAction extends StudioAction {

    private static final String NOTIFICATION_KEY = "Test Connection";

    @Override
    protected boolean isEnabled(@NotNull AnActionEvent e) {
        if (!super.isEnabled(e)) {
            return false;
        }

        // isEnvironmentSelected doesn't decrypt credentials, getSelected does (slow, runs in action update)
        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        return em.isEnvironmentSelected();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment selected = em.getSelected();

        Task.Backgroundable task = new Task.Backgroundable(
                e.getProject(), "Testing connection for '" + selected.getName() + "'") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                new RunnableUtils.PluginClasspathRunnable() {

                    @Override
                    public void runWithPluginClassLoader() {
                        TestConnectionResult result = testConnection(project, selected);

                        handleResult(project, selected, result, () -> {
                            indicator.setText("Reloading caches for '" + selected.getName() + "'");

                            EnvironmentCacheManager.get(project).reload(false, false);
                        });
                    }
                }.run();
            }
        };
        ProgressManager.getInstance()
                .runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    private @Nullable TestConnectionResult testConnection(Project project, Environment environment) {
        try {
            MidPointClient client = new MidPointClient(project, environment);

            return StudioPrismContextService.runSupplierWithProject(project, client::testConnection);
        } catch (Exception ex) {
            RunnableUtils.rethrowIfCancellation(ex);

            MidPointUtils.publishExceptionNotification(project, environment, TestConnectionAction.class,
                    NOTIFICATION_KEY, "Connection test for '" + environment.getName() + "' failed with exception", ex);

            return null;
        }
    }

    /**
     * Publishes the result notification first, the cache reload (slow, many REST calls and prism
     * context rebuild) runs only afterward and only for a successful test.
     */
    @VisibleForTesting
    static void handleResult(Project project, Environment environment, @Nullable TestConnectionResult result, Runnable reloadCaches) {
        if (result == null) {
            return;
        }

        String status = result.success() ? "was successful" : "failed";
        NotificationType type = result.success() ? NotificationType.INFORMATION : NotificationType.ERROR;

        String versionInfo = result.success() ?
                " Version: " + result.version() + ", build: " + result.revision() + "." : "";

        NotificationAction action = null;
        if (result.exception() != null) {
            action = new ShowExceptionNotificationAction("Connection test exception for '" + environment.getName() + "'",
                    result.exception(), TestConnectionAction.class, environment);
        }

        MidPointUtils.publishNotification(project, NOTIFICATION_KEY, "Test connection",
                "Connection test for '" + environment.getName() + "' " + status + "." + versionInfo, type, action);

        if (result.success()) {
            reloadCaches.run();
        }
    }
}
