package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.BackgroundableTask;
import com.evolveum.midpoint.studio.action.task.TestResourceTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestResource extends AsyncAction {

    public static final String ACTION_NAME = "Test Resource";

    public TestResource() {
        super(ACTION_NAME);
    }

    @Override
    protected BackgroundableTask createTask(AnActionEvent e, Environment env) {
        TestResourceTask task = new TestResourceTask(e, env);
        task.setOids(getOids());

        return task;
    }
}
