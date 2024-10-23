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

    public UploadTestResourceAction() {
        super(UploadTestResourceTask.TITLE);
    }

    @Override
    protected UploadExecuteTask createObjectsTask(AnActionEvent e, Environment env) {
        return new UploadTestResourceTask(e.getProject(), e::getDataContext, env);
    }
}
