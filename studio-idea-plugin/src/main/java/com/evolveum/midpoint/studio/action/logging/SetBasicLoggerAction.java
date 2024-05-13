package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.studio.action.task.SetBasicLoggerTask;
import com.evolveum.midpoint.studio.action.task.SetLoggerTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetBasicLoggerAction extends SetLoggerAction {

    private ModuleLogger logger;

    private LoggingLevelType level;

    public SetBasicLoggerAction(ModuleLogger logger, LoggingLevelType level) {
        super("Set to " + level.value());

        this.logger = logger;
        this.level = level;
    }

    @Override
    protected SetLoggerTask createTask(AnActionEvent e, Environment env) {
        SetLoggerTask task = new SetBasicLoggerTask(e.getProject(), logger, level);
        task.setEnvironment(env);

        return task;
    }
}
