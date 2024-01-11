package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.util.ActionUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

public class DownloadMissingNotificationAction extends NotificationAction {

    public DownloadMissingNotificationAction() {
        super("Download missing objects");
    }

    @Override
    public void actionPerformed(AnActionEvent e, Notification notification) {
        Project project = e.getProject();

        List<ObjectReferenceType> objectRefs = new ArrayList<>();
        // todo implement...

        ActionUtils.runDownloadTask(project, objectRefs, false);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
