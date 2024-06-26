package com.evolveum.midpoint.studio.action.task;

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
public class UploadExecuteTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Upload/Execute (raw)";

    public static final String NOTIFICATION_KEY = TITLE;

    public UploadExecuteTask(
            @NotNull Project project, Supplier<DataContext> dataContextSupplier, Environment environment) {

        this(project, dataContextSupplier, environment, TITLE, NOTIFICATION_KEY);
    }

    protected UploadExecuteTask(
            @NotNull Project project, Supplier<DataContext> dataContextSupplier, Environment environment, String title,
            String notificationKey) {

        super(project, dataContextSupplier, title, notificationKey);

        setEnvironment(environment);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject obj) throws Exception {
        UploadTaskMixin.UploadExecuteResult uploadExecuteResult = UploadTaskMixin.uploadExecute(client, obj);

        UploadTaskMixin.showConsoleOutputNotification(
                getProject(), getEnvironment(), getClass(), NOTIFICATION_KEY, obj, uploadExecuteResult);

        return validateOperationResult("upload", uploadExecuteResult.result(), obj.getName());
    }
}
