package com.evolveum.midpoint.studio.action.task;

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
public class UploadRecomputeTask extends UploadExecuteTask {

    public static String TITLE = "Upload/Recompute";

    public static final String NOTIFICATION_KEY = TITLE;

    public UploadRecomputeTask(@NotNull Project project, Supplier<DataContext> dataContextSupplier, Environment environment) {
        super(project, dataContextSupplier, environment, TITLE, NOTIFICATION_KEY);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject obj) throws Exception {
        ProcessObjectResult por = super.processObject(obj);

        OperationResult uploadResult = por.result();

        if (uploadResult != null && !uploadResult.isSuccess()) {
            printProblem("Skipping recomputation for " + obj.getName() + ", there was a problem with upload");

            return por;
        }

        OperationResult recomputeResult = UploadTaskMixin.recompute(client, obj);
        if (recomputeResult == null) {
            return por;
        }

        return validateOperationResult("recompute", recomputeResult, obj.getName());
    }
}
