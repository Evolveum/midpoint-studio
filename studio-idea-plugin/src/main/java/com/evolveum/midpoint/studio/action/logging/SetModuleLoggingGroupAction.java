package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class SetModuleLoggingGroupAction extends DefaultActionGroup implements DumbAware {

    public SetModuleLoggingGroupAction(String logger) {
        addAction(new SetLoggerAction(logger, LoggingLevelType.INFO));
        addAction(new SetLoggerAction(logger, LoggingLevelType.DEBUG));
        addAction(new SetLoggerAction(logger, LoggingLevelType.TRACE));
    }

    protected void addAction(SetLoggerAction action) {
        String id = getClass().getSimpleName() + action.getLevel().value();

        ActionManager am = ActionManager.getInstance();
        am.registerAction(id, action);

        add(action);
    }
}