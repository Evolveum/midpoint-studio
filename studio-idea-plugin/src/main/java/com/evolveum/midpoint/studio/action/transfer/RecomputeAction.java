package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.BackgroundableTask;
import com.evolveum.midpoint.studio.action.task.RecomputeTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RecomputeAction extends AsyncAction {

    public static final String ACTION_NAME = "Recompute action";

    public RecomputeAction() {
        super(ACTION_NAME);
    }

    @Override
    protected BackgroundableTask createTask(AnActionEvent e, Environment env) {
        return new RecomputeTask(e, env);
    }
}
