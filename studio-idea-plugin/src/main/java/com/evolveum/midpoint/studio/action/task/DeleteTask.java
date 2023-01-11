package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.DeleteOptions;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Delete from Server task";

    public static final String NOTIFICATION_KEY = "Delete from Server task";

    private boolean raw;

    public DeleteTask(@NotNull AnActionEvent event, Environment environment) {
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
        String obj = ConfirmationUnit.FILES == unit ? "objects from " : "";
        String raw = this.raw ? " (raw)" : " (non-raw)";

        return String.format("Do you want to delete %s%s %s from server%s?", obj, count, unit.getString(count), raw);
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
