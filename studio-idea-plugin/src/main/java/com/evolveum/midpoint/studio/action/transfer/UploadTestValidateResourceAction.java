package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.UploadExecuteTask;
import com.evolveum.midpoint.studio.action.task.UploadTestValidateResourceTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTestValidateResourceAction extends AsyncObjectsAction {

    public UploadTestValidateResourceAction() {
        super(UploadTestValidateResourceTask.TITLE);
    }

    @Override
    protected UploadExecuteTask createObjectsTask(AnActionEvent e, Environment env) {
        return new UploadTestValidateResourceTask(e.getProject(), e::getDataContext, env);
    }
}
