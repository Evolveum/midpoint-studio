package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.CleanupConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObjects;
import com.evolveum.midpoint.studio.ui.cleanup.MissingObjectRefsDialog;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefMixin;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MissingReferencesNotificationAction extends NotificationAction {

    private static final String TEXT = "Configure missing...";

    private final List<MissingRefObject> data;

    /**
     * @param data list of missing references obtained from cleanup task result.
     */
    public MissingReferencesNotificationAction(@NotNull List<MissingRefObject> data) {
        super(TEXT);

        this.data = data;
    }

    @Override
    public void actionPerformed(AnActionEvent e, Notification notification) {
        Project project = e.getProject();

        // todo get settings and pass proper configuration part to dialog

        MissingObjectRefsDialog dialog = new MissingObjectRefsDialog(project, List.of());
        if (!dialog.showAndGet()) {
            return;
        }

        List<MissingRefObject> result = dialog.getData();

        saveSettings(project, result);
    }

    private void saveSettings(Project project, List<MissingRefObject> result) {
        List<MissingRefObject> cloned = result.stream()
                .map(MissingRefObject::copy)
                .collect(Collectors.toList());

        for (MissingRefObject object : cloned) {
            object.getReferences().removeIf(ref -> ref.getAction() == null);
        }

        CleanupService cs = CleanupService.get(project);

        CleanupConfiguration config = cs.getSettings();
        MissingRefObjects objects = config.getMissingReferences();

        Map<MissingRefMixin.Key, MissingRefObject> existing = objects.getObjects().stream()
                .collect(Collectors.toMap(o -> new MissingRefMixin.Key(o.getOid(), o.getType()), o -> o));

        for (MissingRefObject object : cloned) {
            MissingRefMixin.Key key = new MissingRefMixin.Key(object.getOid(), object.getType());
            MissingRefObject existingObject = existing.get(key);
            if (existingObject != null) {
                existingObject.getReferences().clear();
                existingObject.getReferences().addAll(object.getReferences());
            } else {
                objects.getObjects().add(object);
            }
        }

        cs.settingsUpdated();
    }

    private List<ObjectReferenceType> createRefsForDownload(List<MissingRefObject> objects) {
        return List.of(); // todo implement
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
