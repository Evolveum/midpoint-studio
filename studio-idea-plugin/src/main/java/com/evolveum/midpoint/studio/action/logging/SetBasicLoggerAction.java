package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ClassLoggerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetBasicLoggerAction extends SetLoggerAction {

    private ModuleLogger logger;

    private LoggingLevelType level;

    public SetBasicLoggerAction(ModuleLogger logger, LoggingLevelType level) {
        this.logger = logger;
        this.level = level;
    }

    public String getLogger() {
        return logger.getLogger();
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
    public List<ClassLoggerConfigurationType> buildClassLoggers() {
        ClassLoggerConfigurationType config = new ClassLoggerConfigurationType();
        config.setPackage(logger.getLogger());
        config.setLevel(level);

        return Arrays.asList(config);
    }
}
