package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.UploadExecuteTask;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadFullProcessingAction extends AsyncObjectsAction {

    public static final String ACTION_NAME = "Upload (Full Processing)";

    public UploadFullProcessingAction() {
        super(ACTION_NAME);
    }

    @Override
    protected UploadFullProcessingTask createObjectsTask(AnActionEvent e, Environment env) {
        return new UploadFullProcessingTask(e, env);
    }
}
