package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTestResourceTask extends UploadExecuteTask {

    public static String TITLE = "Upload/Test resource task";

    public static final String NOTIFICATION_KEY = TITLE;

    public static final String OPERATION_TEST_CONNECTION = "test connection";

    public UploadTestResourceTask(AnActionEvent event, Environment environment) {
        this(event, environment, TITLE, NOTIFICATION_KEY);
    }

    protected UploadTestResourceTask(AnActionEvent event, Environment environment, String title, String notificationKey) {
        super(event, environment, title, notificationKey);
    }

    @Override
    protected boolean shouldSkipObjectProcessing(MidPointObject object) {
        return !ObjectTypes.RESOURCE.equals(object.getType());
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject obj) throws Exception {
        ProcessObjectResult por = super.processObject(obj);

        if (obj.isExecutable()) {
            return por;
        }

        OperationResult uploadResult = por.result();

        if (uploadResult != null && !uploadResult.isSuccess()) {
            printProblem("Skipping test connection for " + obj.getName() + ", there was a problem with upload");
            return por;
        }

        if (!ObjectTypes.RESOURCE.equals(obj.getType())) {
            printProblem("Can't test connection for " + obj.getName() + ", because it's not resource, it's " + obj.getType().getClassDefinition().getName());
            return por;
        }

        OperationResult testConnectionResult = client.testResource(obj.getOid());
        return validateOperationResult(OPERATION_TEST_CONNECTION, testConnectionResult, obj.getName());
    }
}
