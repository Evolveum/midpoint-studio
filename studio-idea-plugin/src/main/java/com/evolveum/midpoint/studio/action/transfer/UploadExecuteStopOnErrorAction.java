package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.UploadExecuteStopOnErrorTask;
import com.evolveum.midpoint.studio.action.task.UploadExecuteTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteStopOnErrorAction extends AsyncObjectsAction {

    public UploadExecuteStopOnErrorAction() {
        super(UploadExecuteStopOnErrorTask.TITLE);
    }

    @Override
    protected UploadExecuteTask createObjectsTask(AnActionEvent e, Environment env) {
        return new UploadExecuteStopOnErrorTask(e.getProject(), e::getDataContext, env);
    }
}
