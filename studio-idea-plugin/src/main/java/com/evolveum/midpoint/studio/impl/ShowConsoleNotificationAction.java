package com.evolveum.midpoint.studio.impl;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ShowConsoleNotificationAction extends NotificationAction {

    private static final String TEXT = "Show console";

    private final Project project;

    public ShowConsoleNotificationAction(Project project) {
        super(TEXT);

        this.project = project;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt, @NotNull Notification notification) {
        ConsoleService.get(project).focusConsole();
    }
}
