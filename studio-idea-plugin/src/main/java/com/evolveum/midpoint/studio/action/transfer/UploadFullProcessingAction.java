package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadFullProcessingAction extends AsyncObjectsAction {

    public UploadFullProcessingAction() {
        super(UploadFullProcessingTask.TITLE);
    }

    @Override
    protected UploadFullProcessingTask createObjectsTask(AnActionEvent e, Environment env) {
        return new UploadFullProcessingTask(e.getProject(), e::getDataContext, env);
    }
}
