package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.DownloadTask;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
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

        EnvironmentService es = EnvironmentService.getInstance(project);
        Environment env = es.getSelected();

        if (env == null) {
            MidPointUtils.publishNotification(
                    project, "Download missing", "Error", "No environment selected", NotificationType.WARNING);
            return;
        }

        List<Pair<String, ObjectTypes>> objectRefs = new ArrayList<>();

        DownloadTask task = new DownloadTask(e, objectRefs, null, null, false, true, false);
        task.setEnvironment(env);
        task.setOpenAfterDownload(false);

        ProgressManager.getInstance().run(task);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
