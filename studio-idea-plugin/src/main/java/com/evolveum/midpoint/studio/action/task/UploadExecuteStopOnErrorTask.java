package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteStopOnErrorTask extends UploadExecuteTask {

    public UploadExecuteStopOnErrorTask(AnActionEvent event, Environment environment) {
        super(event, environment);
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
