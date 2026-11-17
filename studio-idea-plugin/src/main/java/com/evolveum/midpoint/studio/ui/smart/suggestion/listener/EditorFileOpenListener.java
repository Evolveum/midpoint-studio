package com.evolveum.midpoint.studio.ui.smart.suggestion.listener;

import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

/**
 * Offers the smart suggestion tool window when a resource definition is opened in a project
 * with the MidPoint facet and a selected environment. "Don't show again" is persisted per project.
 */
public class EditorFileOpenListener implements FileEditorManagerListener {

    static final String NOTIFICATION_GROUP = "midpointSmartSuggestion";

    static final String TOOL_WINDOW_ID = "SmartSuggestionToolWindow";

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        Project project = source.getProject();

        if (shouldShowNotification(project, file)) {
            showSuggestionNotification(project, file);
        }
    }

    static boolean shouldShowNotification(Project project, VirtualFile file) {
        if (project == null || project.isDisposed() || !file.isValid()) {
            return false;
        }

        if (!MidPointUtils.hasMidPointFacet(project)) {
            return false;
        }

        if (MidPointService.get(project).getSettings().isHideSmartSuggestionNotification()) {
            return false;
        }

        if (!EnvironmentService.getInstance(project).isEnvironmentSelected()) {
            return false;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        return MidPointUtils.findResourceOidByPsi(psiFile) != null;
    }

    private void showSuggestionNotification(Project project, @NotNull VirtualFile file) {
        Notification notification = new Notification(
                NOTIFICATION_GROUP,
                "Midpoint Smart suggestion available",
                "Generate smart suggestions for %s resource midpoint XML configuration.".formatted(file.getName()),
                NotificationType.INFORMATION
        );

        notification.addAction(new NotificationAction("Generate smart suggestion") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                Project p = e.getProject();
                if (p == null) {
                    return;
                }

                ToolWindow toolWindow = ToolWindowManager.getInstance(p).getToolWindow(TOOL_WINDOW_ID);
                if (toolWindow != null) {
                    toolWindow.show(notification::expire);
                }
            }
        });

        notification.addAction(new NotificationAction("Don't show again") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                hideNotification(project);
                notification.expire();
            }
        });

        Notifications.Bus.notify(notification, project);
    }

    static void hideNotification(Project project) {
        MidPointService ms = MidPointService.get(project);
        ms.getSettings().setHideSmartSuggestionNotification(true);
        ms.settingsUpdated();
    }
}
