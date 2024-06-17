package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.cleanup.MissingObjectRefsDialog;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefKey;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MissingReferencesNotificationAction extends NotificationAction {

    private static final String TEXT = "Configure missing...";

    private final List<MissingRefObject> foundRefs;

    /**
     * @param foundRefs list of missing references obtained from cleanup task result.
     */
    public MissingReferencesNotificationAction(@NotNull List<MissingRefObject> foundRefs) {
        super(TEXT);

        this.foundRefs = foundRefs;
    }

    @Override
    public void actionPerformed(AnActionEvent e, Notification notification) {
        Project project = e.getProject();

        List<MissingRefObject> clonedFoundRefs =
                MissingRefUtils.populateCurrentActionsForMissingRefs(project, foundRefs);

        MissingObjectRefsDialog dialog = new MissingObjectRefsDialog(project, clonedFoundRefs);
        if (!dialog.showAndGet()) {
            return;
        }

        List<MissingRefObject> result = dialog.getData();

        List<MissingRefKey> removed = MissingRefUtils.computeRemovedRefKeys(clonedFoundRefs, result);

        MissingRefUtils.updateMissingRefSettings(project, result, removed);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
