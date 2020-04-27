package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetLoggerAction extends AnAction {

    private String logger;

    private LoggingLevelType level;

    public SetLoggerAction(String logger, LoggingLevelType level) {
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // todo implement
    }
}
