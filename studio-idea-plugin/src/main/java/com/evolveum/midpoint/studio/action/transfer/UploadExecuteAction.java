package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.UploadExecuteTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteAction extends AsyncAction {

    public static final String ACTION_NAME = "Upload/execute";

    public UploadExecuteAction() {
        super(ACTION_NAME);
    }

    @Override
    protected UploadExecuteTask createTask(AnActionEvent e, Environment env) {
        return new UploadExecuteTask(e, env);
    }
}
