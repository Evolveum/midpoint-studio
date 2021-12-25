package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.UploadRecomputeTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadRecomputeAction extends AsyncObjectsAction {

    public static final String ACTION_NAME = "Upload/recompute";

    public UploadRecomputeAction() {
        super(ACTION_NAME);
    }

    @Override
    protected UploadRecomputeTask createTask(AnActionEvent e, Environment env) {
        return new UploadRecomputeTask(e, env);
    }
}
