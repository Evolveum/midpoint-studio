package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncObjectsAction;
import com.evolveum.midpoint.studio.action.task.ObjectsBackgroundableTask;
import com.evolveum.midpoint.studio.action.task.TestResourceTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestResource extends AsyncObjectsAction {

    public TestResource() {
        super(TestResourceTask.TITLE);
    }

    @Override
    protected ObjectsBackgroundableTask createObjectsTask(AnActionEvent e, Environment env) {
        return new TestResourceTask(e.getProject(), e::getDataContext, env);
    }
}
