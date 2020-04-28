package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetBasicLoggerAction extends SetLoggerAction {

    private String logger;

    private LoggingLevelType level;

    public SetBasicLoggerAction(String logger, LoggingLevelType level) {
        this.logger = logger;
        this.level = level;
    }

    public String getLogger() {
        return logger;
    }

    public LoggingLevelType getLevel() {
        return level;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setText("Set to " + level.value());
    }
}
