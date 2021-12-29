package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.studio.action.task.SetLoggerTask;
import com.evolveum.midpoint.studio.action.task.SetModelLoggerTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetModelLoggerAction extends SetLoggerAction {

    private ModelLogger logger;

    public SetModelLoggerAction(ModelLogger logger) {
        super("Set to " + logger.getLabel());

        this.logger = logger;
    }

    public ModelLogger getLogger() {
        return logger;
    }

    @Override
    protected SetLoggerTask createTask(AnActionEvent e, Environment env) {
        SetLoggerTask task = new SetModelLoggerTask(e, logger);
        task.setEnvironment(env);

        return task;
    }
}
