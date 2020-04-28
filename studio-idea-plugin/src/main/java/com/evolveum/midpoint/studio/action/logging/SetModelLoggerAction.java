package com.evolveum.midpoint.studio.action.logging;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetModelLoggerAction extends SetLoggerAction {

    private ModelLogger logger;

    public SetModelLoggerAction(ModelLogger logger) {
        this.logger = logger;
    }

    public ModelLogger getLogger() {
        return logger;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setText("Set to " + logger.getLabel());
    }
}
