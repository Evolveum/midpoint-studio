package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.studio.action.task.SetAllToInfoTask;
import com.evolveum.midpoint.studio.action.task.SetLoggerTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetAllToInfoAction extends SetLoggerAction {

    public SetAllToInfoAction() {
        this(null);
    }

    public SetAllToInfoAction(String text) {
        super(text);
    }

    @Override
    protected SetLoggerTask createTask(AnActionEvent e, Environment env) {
        SetLoggerTask task = new SetAllToInfoTask(e);
        task.setEnvironment(env);

        return task;
    }
}
