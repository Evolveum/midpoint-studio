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
public class UploadExecuteStopOnErrorTask extends UploadExecuteTask {

    public static String TITLE = "Upload/Execute (raw, stop on error)";

    public static final String NOTIFICATION_KEY = TITLE;

    public UploadExecuteStopOnErrorTask(
            @NotNull Project project, Supplier<DataContext> dataContextSupplier, Environment environment) {

        super(project, dataContextSupplier, environment, TITLE, NOTIFICATION_KEY);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject obj) throws Exception {
        try {
            ProcessObjectResult por = super.processObject(obj);
            if (por.problem()) {
                por.shouldContinue(false);
            }

            return por;
        } catch (Exception ex) {
            return new ProcessObjectResult(null).problem(true).shouldContinue(false);
        }
    }
}
