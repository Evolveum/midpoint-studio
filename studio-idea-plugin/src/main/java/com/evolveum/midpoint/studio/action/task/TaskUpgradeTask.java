package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.RefreshAction;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskUpgradeTask extends BackgroundableTask<TaskState> {

    public static String TITLE = "Upgrade task";

    public static final String NOTIFICATION_KEY = "Upgrade task";

    private static final Logger LOG = Logger.getInstance(TaskUpgradeTask.class);

    public TaskUpgradeTask(AnActionEvent event) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
    }

    protected void processEditorText(ProgressIndicator indicator, Editor editor, String text, VirtualFile sourceFile) {
        try {
            List<MidPointObject> objects = MidPointUtils.parseText(getProject(), text, getNotificationKey());

            List<String> newObjects = processObjects(objects, sourceFile);

            boolean isUpdateSelection = isUpdateSelectionInEditor(editor);

            StringBuilder sb = new StringBuilder();
            if (!isUpdateSelection && newObjects.size() > 1) {
                sb.append(ClientUtils.OBJECTS_XML_PREFIX);
            }

            newObjects.forEach(o -> sb.append(o));

            if (!isUpdateSelection && newObjects.size() > 1) {
                sb.append(ClientUtils.OBJECTS_XML_SUFFIX);
            }

            updateEditor(editor, sb.toString());
        } catch (Exception ex) {
            // todo fix
            ex.printStackTrace();
        }
    }

    protected void processFile(VirtualFile file) {
        List<MidPointObject> objects = loadObjectsFromFile(file);

        List<String> newObjects = processObjects(objects, file);

        writeObjectsToFile(file, newObjects);
    }

    private List<String> processObjects(List<MidPointObject> objects, VirtualFile file) {
        if (objects.isEmpty()) {
            state.incrementSkippedFile();
            midPointService.printToConsole(null, RefreshAction.class,
                    "Skipped file " + (file != null ? file.getPath() : "<unknown>") + " no objects found (parsed).");

            return Collections.emptyList();
        }

        List<String> newObjects = new ArrayList<>();

        for (MidPointObject object : objects) {
            ProgressManager.checkCanceled();

            if (!ObjectTypes.TASK.equals(object.getType())) {
                newObjects.add(object.getContent());
                state.incrementSkipped();
            }

            try {
                String newContent = MidPointUtils.upgradeTaskToUseActivities(object.getContent());
                newObjects.add(newContent);

                state.incrementProcessed();
            } catch (Exception ex) {
                state.incrementFailed();
                newObjects.add(object.getContent());

                midPointService.printToConsole(null, RefreshAction.class, "Error upgrading task"
                        + object.getName() + "(" + object.getOid() + ")", ex);
            }
        }

        return newObjects;
    }
}
