package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.cleanup.MissingObjectRefsDialog;
import com.evolveum.midpoint.studio.ui.configuration.MissingRefObjectsConfigurable;
import com.evolveum.midpoint.studio.util.ActionUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MissingReferencesNotificationAction extends NotificationAction {

    private static final String TEXT = "Configure missing...";

    private final List<MissingRefObject> data;

    private final boolean summary;

    public MissingReferencesNotificationAction(@NotNull List<MissingRefObject> data, boolean summary) {
        super(TEXT);

        this.data = data;
        this.summary = summary;
    }

    @Override
    public void actionPerformed(AnActionEvent e, Notification notification) {
        Project project = e.getProject();

        if (summary) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, MissingRefObjectsConfigurable.class);
            return;
        }

        // todo get settings and pass proper configuration part to dialog

        MissingObjectRefsDialog dialog = new MissingObjectRefsDialog(project, List.of());
        if (!dialog.showAndGet()) {
            return;
        }

        List<ObjectReferenceType> references = createRefsForDownload(dialog.getData());
        if (references.isEmpty()) {
            return;
        }

        ActionUtils.runDownloadTask(project, references, false);
    }

    private List<ObjectReferenceType> createRefsForDownload(List<MissingRefObject> objects) {
        return List.of(); // todo implement
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
