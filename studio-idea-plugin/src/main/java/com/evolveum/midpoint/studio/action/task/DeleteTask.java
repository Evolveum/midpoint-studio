package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.DeleteOptions;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Delete task";

    public static final String NOTIFICATION_KEY = "Delete task";

    private boolean raw;

    public DeleteTask(AnActionEvent event, Environment environment) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
        setEnvironment(environment);
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    @Override
    protected String getConfirmationMessage(int count, ConfirmationUnit unit) {
        return "Do you want to delete " + count + " " + unit.getString(count) + "?";
    }

    @Override
    protected String getConfirmationYesActionText() {
        return "Delete";
    }

    @Override
    protected boolean isProcessingOnlyObjectTypes() {
        return true;
    }

    @Override
    protected boolean isUpdateObjectAfterProcessing() {
        return false;
    }

    @Override
    public boolean isShowConfirmationDialog() {
        return true;
    }

    @Override
    public ProcessObjectResult processObjectOid(ObjectTypes type, String oid) throws Exception {
        DeleteOptions opts = new DeleteOptions().raw(raw);

        client.delete(type.getClassDefinition(), oid, opts);

        return new ProcessObjectResult(null);
    }
}
