package com.evolveum.midpoint.studio.action.logging;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ModelLoggingGroupAction extends SetModuleLoggingGroupAction {

    public ModelLoggingGroupAction() {
        super(ModuleLogger.MODEL);

        add(new Separator());

        for (ModelLogger ml : ModelLogger.values()) {
            addAction(new SetModelLoggerAction(ml));
        }
    }

    protected void addAction(SetModelLoggerAction action) {
        String id = getClass().getSimpleName() + "." + action.getLogger().name();

        ActionManager am = ActionManager.getInstance();
        AnAction a = am.getAction(id);
        if (a == null) {
            am.registerAction(id, action);
            a = action;
        }

        add(action);
    }
}
