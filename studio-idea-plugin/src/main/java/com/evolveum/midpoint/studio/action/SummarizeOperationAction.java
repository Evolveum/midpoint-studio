package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class SummarizeOperationAction extends AsyncAction<SummarizeOperationsTask> {

    public SummarizeOperationAction() {
        super(SummarizeOperationsTask.TITLE);
    }

    @Override
    protected SummarizeOperationsTask createTask(AnActionEvent e, Environment env) {
        SummarizeOperationsTask task = new SummarizeOperationsTask(e.getProject(), e::getDataContext);
        task.setEnvironment(env);
        
        return task;
    }
}
