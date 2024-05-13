package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.UploadExecuteTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteAction extends AsyncObjectsAction {

    public static final String ACTION_NAME = "Upload/execute";

    public UploadExecuteAction() {
        super(ACTION_NAME);
    }

    @Override
    protected UploadExecuteTask createObjectsTask(AnActionEvent e, Environment env) {
        return new UploadExecuteTask(e.getProject(), e::getDataContext, env);
    }
}
