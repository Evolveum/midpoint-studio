package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Upload/Execute task";

    public static final String NOTIFICATION_KEY = TITLE;

    public UploadExecuteTask(AnActionEvent event, Environment environment) {
        this(event, environment, TITLE, NOTIFICATION_KEY);
    }

    protected UploadExecuteTask(AnActionEvent event, Environment environment, String title, String notificationKey) {
        super(event.getProject(), title, notificationKey);

        setEvent(event);
        setEnvironment(environment);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject obj) throws Exception {
        OperationResult result = UploadTaskMixin.uploadExecute(client, obj);

        return validateOperationResult("upload", result, obj.getName());
    }
}
