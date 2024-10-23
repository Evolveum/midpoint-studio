package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.action.logging.ModuleLogger;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ClassLoggerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetBasicLoggerTask extends SetLoggerTask {

    private ModuleLogger logger;

    private LoggingLevelType level;

    public SetBasicLoggerTask(@NotNull Project project, @NotNull ModuleLogger logger, @NotNull LoggingLevelType level) {
        super(project, "Set to " + level.value(), "Set to " + level.value());

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
    public List<ClassLoggerConfigurationType> buildClassLoggers() {
        ClassLoggerConfigurationType config = new ClassLoggerConfigurationType();
        config.setPackage(logger.getLogger());
        config.setLevel(level);

        return Arrays.asList(config);
    }
}
