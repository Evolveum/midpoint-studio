package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.RefreshAction;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    protected List<String> processObjects(List<MidPointObject> objects, VirtualFile file) {
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
                String oldContent = object.getContent();
                String newContent = MidPointUtils.upgradeTaskToUseActivities(oldContent);
                newObjects.add(newContent);

                if (Objects.equals(oldContent, newContent)) {
                    state.incrementSkipped();

                    midPointService.printToConsole(null, TaskUpgradeTask.class,
                            "Skipped object " + object.getName() + "(" + object.getOid() + ", " + (file != null ? file.getName() : "unknown") + ")");
                } else {
                    state.incrementProcessed();
                }
            } catch (Exception ex) {
                state.incrementFailed();
                newObjects.add(object.getContent());

                midPointService.printToConsole(null, TaskUpgradeTask.class, "Error upgrading task"
                        + object.getName() + "(" + object.getOid() + ")", ex);
            }
        }

        return newObjects;
    }
}
