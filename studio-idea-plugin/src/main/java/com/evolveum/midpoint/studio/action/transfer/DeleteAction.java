package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.DeleteTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteAction extends AsyncAction {

    public static final String ACTION_NAME = "Delete action";

    private boolean raw;

    public DeleteAction() {
        super(ACTION_NAME);
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    @Override
    protected DeleteTask createTask(AnActionEvent e, Environment env) {
        DeleteTask task = new DeleteTask(e, env);
        task.setRaw(raw);
        task.setOids(getOids());

        return task;
    }
}
