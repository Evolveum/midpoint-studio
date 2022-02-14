package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.action.logging.ModelLogger;
import com.evolveum.midpoint.studio.action.logging.ModuleLogger;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ClassLoggerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetAllToInfoTask extends SetLoggerTask {

    public static String TITLE = "Set all to INFO task";

    public static final String NOTIFICATION_KEY = "Set all to INFO task";

    public SetAllToInfoTask(@NotNull AnActionEvent event) {
        super(event, TITLE, NOTIFICATION_KEY);
    }

    @Override
    public List<ClassLoggerConfigurationType> buildClassLoggers() {
        List<ClassLoggerConfigurationType> list = new ArrayList<>();

        Arrays.stream(ModuleLogger.values()).forEach(o -> list.add(createClassLogger(o.getLogger(), LoggingLevelType.INFO)));
        Arrays.stream(ModelLogger.values()).forEach(o -> list.add(createClassLogger(o.getLogger(), LoggingLevelType.INFO)));

        return list;
    }
}
