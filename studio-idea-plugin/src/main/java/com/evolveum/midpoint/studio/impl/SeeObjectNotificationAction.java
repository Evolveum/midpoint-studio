package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class SeeObjectNotificationAction extends NotificationAction {

    private final VirtualFile file;

    public SeeObjectNotificationAction(VirtualFile file) {
        super("See file");

        this.file = file;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull Notification notification) {
        if (file == null || !file.exists()) {
            return;
        }

        RunnableUtils.runWriteActionAndWait(() -> {
            MidPointUtils.openFile(event.getProject(), file);
        });
    }
}
