package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTestResourceTask extends UploadExecuteTask {

    public static String TITLE = "Upload/Test Resource";

    public static final String NOTIFICATION_KEY = TITLE;

    public static final String OPERATION_TEST_CONNECTION = "test connection";

    public UploadTestResourceTask(
            @NotNull Project project, Supplier<DataContext> dataContextSupplier, Environment environment) {

        this(project, dataContextSupplier, environment, TITLE, NOTIFICATION_KEY);
    }

    protected UploadTestResourceTask(
            @NotNull Project project, Supplier<DataContext> dataContextSupplier, Environment environment, String title,
            String notificationKey) {

        super(project, dataContextSupplier, environment, title, notificationKey);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject obj) throws Exception {
        ProcessObjectResult por = super.processObject(obj);

        OperationResult uploadResult = por.result();

        if (uploadResult != null && !uploadResult.isSuccess()) {
            printProblem("Skipping test connection for " + obj.getName() + ", there was a problem with upload");
            return por;
        }

        if (!ObjectTypes.RESOURCE.equals(obj.getType())) {
            printProblem("Skipping test connection for " + obj.getName() + ", it's not a resource");

            OperationResult result = new OperationResult(OPERATION_TEST_CONNECTION);
            result.recordWarning("Skipping test connection for " + obj.getName() + ", it's not a resource");
            por.result(result);
            por.problem(true);

            return por;
        }

        OperationResult testConnectionResult = UploadTaskMixin.testResourceConnection(client, obj);
        if (testConnectionResult == null) {
            return por;
        }

        return validateOperationResult(OPERATION_TEST_CONNECTION, testConnectionResult, obj.getName());
    }
}
