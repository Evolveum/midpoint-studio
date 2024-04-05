package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.*;
import com.evolveum.midpoint.studio.ui.cleanup.MissingObjectRefsDialog;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefKey;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        List<MissingRefObject> cloned = data.stream()
                .map(MissingRefObject::copy)
                .collect(Collectors.toList());

        CleanupService cs = CleanupService.get(project);
        MissingRefObjects settings = cs.getSettings().getMissingReferences();

        Map<MissingRefKey, MissingRefObject> existing = settings.getObjects().stream()
                .collect(Collectors.toMap(o -> new MissingRefKey(o.getOid(), o.getType()), o -> o));

        for (MissingRefObject clonedObject : cloned) {
            MissingRefKey key = new MissingRefKey(clonedObject.getOid(), clonedObject.getType());
            MissingRefObject existingObject = existing.get(key);

            Map<MissingRefKey, MissingRefAction> existingRefs = existingObject != null ?
                    existingObject.getReferences().stream()
                            .collect(Collectors.toMap(o -> new MissingRefKey(o.getOid(), o.getType()), o -> o.getAction())) :
                    new HashMap<>();

            clonedObject.getReferences().forEach(mr -> {
                MissingRefKey refKey = new MissingRefKey(mr.getOid(), mr.getType());
                MissingRefAction action = existingRefs.get(refKey);
                if (action != null) {
                    mr.setAction(action);
                }
            });
        }

        MissingObjectRefsDialog dialog = new MissingObjectRefsDialog(project, cloned);
        if (!dialog.showAndGet()) {
            return;
        }

        List<MissingRefObject> result = dialog.getData();

        saveSettings(project, cloned, result);
    }

    /**
     * Save settings to project configuration file (midpoint.xml).
     * <p>
     * Replaces existing {@link MissingRefObject} object with the ones from result list.
     * Removes {@link MissingRefObject} objects that were removed in the editor dialog by matching against input.
     */
    private void saveSettings(Project project, List<MissingRefObject> input, List<MissingRefObject> result) {
        Set<MissingRefKey> resultKeys = result.stream()
                .map(o -> new MissingRefKey(o.getOid(), o.getType()))
                .collect(Collectors.toSet());

        List<MissingRefObject> cloned = MissingRefUtils.cloneAndRemoveNonActionable(result);

        CleanupService cs = CleanupService.get(project);

        CleanupConfiguration config = cs.getSettings();
        MissingRefObjects objects = config.getMissingReferences();

        Map<MissingRefKey, MissingRefObject> existing = objects.getObjects().stream()
                .collect(Collectors.toMap(o -> new MissingRefKey(o.getOid(), o.getType()), o -> o));

        // replace objects in settings with the ones from the dialog
        for (MissingRefObject object : cloned) {
            MissingRefKey key = new MissingRefKey(object.getOid(), object.getType());

            MissingRefObject existingObject = existing.get(key);
            if (existingObject != null) {
                objects.getObjects().remove(existingObject);
            }

            objects.getObjects().add(object);
        }

        // remove objects from settings that were removed in the dialog
        for (MissingRefObject object : input) {
            MissingRefKey key = new MissingRefKey(object.getOid(), object.getType());

            if (!resultKeys.contains(key)) {
                MissingRefObject existingObject = existing.get(key);
                if (existingObject != null) {
                    objects.getObjects().remove(existingObject);
                }
            }
        }

        cs.settingsUpdated();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
