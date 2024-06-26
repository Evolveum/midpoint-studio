package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.action.logging.ModelLogger;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ClassLoggerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetModelLoggerTask extends SetLoggerTask {

    private ModelLogger logger;

    public SetModelLoggerTask(@NotNull Project project, @NotNull ModelLogger logger) {
        super(project, "Set to " + logger.getLabel(), "Set to " + logger.getLabel());

        this.logger = logger;
    }

    public ModelLogger getLogger() {
        return logger;
    }

    @Override
    public List<ClassLoggerConfigurationType> buildClassLoggers() {
        return createModelLoggers(logger);
    }

    protected List<ClassLoggerConfigurationType> createModelLoggers(ModelLogger logger) {
        List<ClassLoggerConfigurationType> list = new ArrayList<>();

        switch (logger) {
            case LENS_TRACE:
                list.add(createClassLogger(ModelLogger.LENS_TRACE.getLogger(), LoggingLevelType.TRACE));
            case PROJECTOR_TRACE:
                list.add(createClassLogger(ModelLogger.PROJECTOR_TRACE.getLogger(), LoggingLevelType.TRACE));
            case EXPRESSION_TRACE:
                list.add(createClassLogger(ModelLogger.EXPRESSION_TRACE.getLogger(), LoggingLevelType.TRACE));
            case MAPPING_TRACE:
                list.add(createClassLogger(ModelLogger.MAPPING_TRACE.getLogger(), LoggingLevelType.TRACE));
            case PROJECTOR_SUMMARY:
                list.add(createClassLogger(ModelLogger.PROJECTOR_SUMMARY.getLogger(), LoggingLevelType.TRACE));
            case CLOCKWORK_SUMMARY:
                list.add(createClassLogger(ModelLogger.CLOCKWORK_SUMMARY.getLogger(), LoggingLevelType.DEBUG));
        }

        return list;
    }
}
