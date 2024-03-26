package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.util.ActionUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DownloadMissingNotificationAction extends NotificationAction {

    private List<ObjectReferenceType> references;

    public DownloadMissingNotificationAction(@NotNull List<ObjectReferenceType> references) {
        super("Download missing objects");

        this.references = references;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setVisible(!references.isEmpty());
    }

    @Override
    public void actionPerformed(AnActionEvent e, Notification notification) {
        Project project = e.getProject();

        ActionUtils.runDownloadTask(project, references, false);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
