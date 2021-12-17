package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.DeleteOptions;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Delete task";

    public static final String NOTIFICATION_KEY = "Delete task";

    private static final Logger LOG = Logger.getInstance(DeleteTask.class);

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
    public <O extends ObjectType> ProcessObjectResult processObjectOid(ObjectTypes type, String oid) throws Exception {
        DeleteOptions opts = new DeleteOptions().raw(raw);

        client.delete(type.getClassDefinition(), oid, opts);

        Thread.sleep(5000);

        return new ProcessObjectResult(null);
    }
}
