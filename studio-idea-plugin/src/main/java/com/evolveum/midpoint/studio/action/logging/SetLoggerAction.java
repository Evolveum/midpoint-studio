package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.SetLoggerTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetLoggerAction extends AsyncAction<SetLoggerTask> {

    private static final String ACTION_NAME = "Update logging configuration";

    public SetLoggerAction() {
        this(ACTION_NAME);
    }

    public SetLoggerAction(String text) {
        super(text);
    }

    @Override
    protected SetLoggerTask createTask(AnActionEvent e, Environment env) {
        Project project = e.getProject();
        if (project == null) {
            return null;
        }

        SetLoggerTask task = new SetLoggerTask(project);
        task.setEnvironment(env);

        return task;
    }
}
