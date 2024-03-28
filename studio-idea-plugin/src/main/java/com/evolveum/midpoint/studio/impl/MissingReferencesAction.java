package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.ObjectReferencesConfiguration;
import com.evolveum.midpoint.studio.ui.cleanup.MissingObjectRefsDialog;
import com.evolveum.midpoint.studio.util.ActionUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MissingReferencesAction extends NotificationAction {

    private final List<ObjectReferenceType> data;

    public MissingReferencesAction(@NotNull List<ObjectReferenceType> data) {
        super("Fix missing objects...");

        this.data = data;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        // todo fix, this doesn't work! it's not even called. same for download missing notification action
//        e.getPresentation().setVisible(!references.isEmpty());
    }

    @Override
    public void actionPerformed(AnActionEvent e, Notification notification) {
        Project project = e.getProject();


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

    private List<ObjectReferenceType> createRefsForDownload(List<ObjectReferencesConfiguration> objects) {
        return List.of(); // todo implement
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
