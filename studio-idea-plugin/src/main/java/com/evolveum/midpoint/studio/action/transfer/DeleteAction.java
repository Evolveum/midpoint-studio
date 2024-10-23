package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.DeleteTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteAction extends AsyncObjectsAction {

    public static final String ACTION_NAME = "Delete from Server";

    private boolean raw;

    public DeleteAction() {
        this(ACTION_NAME);
    }

    public DeleteAction(String name) {
        super(name);
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    @Override
    protected DeleteTask createObjectsTask(AnActionEvent e, Environment env) {
        DeleteTask task = new DeleteTask(e.getProject(), e::getDataContext, env);
        task.setRaw(raw);

        return task;
    }
}
