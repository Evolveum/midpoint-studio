package com.evolveum.midpoint.studio.ui.smart.suggestion.listener;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class EditorFileOpenListener implements FileEditorManagerListener {

    @Override
    public void fileOpened(@NotNull FileEditorManager source,
                           @NotNull VirtualFile file) {

        Project project = source.getProject();
        PsiFile psiFile = PsiManager.getInstance(project).findFile(
                source.getSelectedFiles().length > 0 ? file : source.getSelectedFiles()[0]);

        if (MidPointUtils.findResourceOidByPsi(psiFile) != null) {
            showSuggestionNotification(project, file);
        }
    }

    private void showSuggestionNotification(Project project, @NotNull VirtualFile file) {

        Notification notification = new Notification(
                "midpointSmartSuggestion",
                "Midpoint Smart suggestion available",
                "Generate smart suggestions for %s resource midpoint XML configuration.".formatted(file.getName()),
                NotificationType.INFORMATION
        );

        notification.addAction(new NotificationAction("Generate smart suggestion") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent, @NotNull Notification notification) {
                ToolWindow toolWindow = ToolWindowManager.getInstance(Objects.requireNonNull(anActionEvent.getProject()))
                        .getToolWindow("SmartSuggestionToolWindow");

                if (toolWindow != null) {
                    toolWindow.show(notification::expire);
                }
            }
        });

        notification.addAction(new NotificationAction("Don't show again") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                notification.expire();
            }
        });

        Notifications.Bus.notify(notification, project);
    }
}
