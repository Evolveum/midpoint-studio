package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefUtils;
import com.evolveum.midpoint.studio.util.ActionUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Download missing references obtained via constructor.
 * They are filtered based on missing ref configuration before download.
 */
public class DownloadMissingNotificationAction extends NotificationAction {

    private static final String TEXT = "Download missing";

    private final Project project;

    private final List<MissingRefObject> data;

    /**
     * @param data list of missing references from cleanup task. It will be used to compute
     *             download only references using cleanup configuration.
     */
    public DownloadMissingNotificationAction(
            @NotNull Project project, @NotNull List<MissingRefObject> data) {
        super(TEXT);

        this.project = project;
        this.data = data;
    }

    @Override
    public void actionPerformed(AnActionEvent e, Notification notification) {
        List<ObjectReferenceType> references = MissingRefUtils.computeDownloadOnly(project, data);

        ActionUtils.runDownloadTask(project, references, false);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    public boolean isVisible() {
        return !data.isEmpty();
    }
}
