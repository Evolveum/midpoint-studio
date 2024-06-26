package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskUpgradeTask extends ObjectsBackgroundableTask<TaskState> {

    public static String TITLE = "Upgrade task";

    public static final String NOTIFICATION_KEY = "Upgrade task";

    private static final Logger LOG = Logger.getInstance(TaskUpgradeTask.class);

    public TaskUpgradeTask(@NotNull Project project, Supplier<DataContext> dataContextSupplier) {
        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);
    }

    @Override
    protected boolean isUpdateObjectAfterProcessing() {
        return true;
    }

    @Override
    protected boolean shouldSkipObjectProcessing(MidPointObject object) {
        return !ObjectTypes.TASK.equals(object.getType());
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject object) throws Exception {
        String oldContent = object.getContent();
        String newContent = MidPointUtils.upgradeTaskToUseActivities(oldContent);

        MidPointObject newObject = MidPointObject.copy(object);
        newObject.setContent(newContent);

        return new ProcessObjectResult(null)
                .object(newObject);
    }

    @Override
    public ProcessObjectResult processObjectOid(ObjectTypes type, String oid) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }
}
