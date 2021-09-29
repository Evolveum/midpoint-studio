package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ClassLoggerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;

import java.util.Arrays;
import java.util.List;

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
