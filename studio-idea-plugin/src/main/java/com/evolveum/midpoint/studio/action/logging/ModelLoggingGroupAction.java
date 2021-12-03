package com.evolveum.midpoint.studio.action.logging;

import com.intellij.openapi.actionSystem.Separator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ModelLoggingGroupAction extends SetModuleLoggingGroupAction {

    public ModelLoggingGroupAction() {
        super(ModuleLogger.MODEL);

        add(new Separator());

        for (ModelLogger ml : ModelLogger.values()) {
            add(new SetModelLoggerAction(ml));
        }
    }
}
