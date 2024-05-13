package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.UploadExecuteTask;
import com.evolveum.midpoint.studio.action.task.UploadTestResourceTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by lazyman on 10/02/2017.
 */
public class UploadTestResourceAction extends AsyncObjectsAction {

    public static final String ACTION_NAME = "Upload/Test Resource";

    public UploadTestResourceAction() {
        super(ACTION_NAME);
    }

    @Override
    protected UploadExecuteTask createObjectsTask(AnActionEvent e, Environment env) {
        return new UploadTestResourceTask(e.getProject(), e::getDataContext, env);
    }
}
