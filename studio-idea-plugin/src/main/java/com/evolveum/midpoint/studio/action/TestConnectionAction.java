package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.client.api.TestConnectionResult;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentManager;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.ShowExceptionNotificationAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestConnectionAction extends AnAction {

    private static final String NOTIFICATION_KEY = "Test Connection";

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        Project project = e.getProject();
        EnvironmentManager em = EnvironmentManager.getInstance(project);
        Environment selected = em.getSelected();

        e.getPresentation().setEnabled(selected != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        EnvironmentManager em = EnvironmentManager.getInstance(project);
        Environment selected = em.getSelected();

        Task.Backgroundable task = new Task.Backgroundable(e.getProject(), "Testing connection for '" + selected.getName() + "'") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                new RunnableUtils.PluginClasspathRunnable() {

                    @Override
                    public void runWithPluginClassLoader() {
                        testConnection(project, selected);
                    }
                }.run();
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    private void testConnection(Project project, Environment environment) {
        try {
            MidPointClient client = new MidPointClient(project, environment);
            TestConnectionResult result = client.testConnection();

            String status = result.success() ? "was successful" : "failed";
            NotificationType type = result.success() ? NotificationType.INFORMATION : NotificationType.ERROR;

            String versionInfo = "";
            if (result.success()) {
                versionInfo = " Version: " + result.version() + ", build: " + result.revision() + ".";
            }

            NotificationAction action = null;
            if (result.exception() != null) {
                action = new ShowExceptionNotificationAction("Connection test exception for '" + environment.getName() + "'", result.exception());
            }

            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Test connection",
                    "Connection test for '" + environment.getName() + "' " + status + "." + versionInfo, type, action);
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Connection test for '" + environment.getName() + "' failed with exception", ex);
        }
    }
}