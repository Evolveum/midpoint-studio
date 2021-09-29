package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ClassLoggerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetAllToInfoAction extends SetLoggerAction {

    public SetAllToInfoAction() {
        super(null);
    }

    public SetAllToInfoAction(String text) {
        super(text);
    }

    @Override
    public List<ClassLoggerConfigurationType> buildClassLoggers() {
        List<ClassLoggerConfigurationType> list = new ArrayList<>();

        Arrays.stream(ModuleLogger.values()).forEach(o -> list.add(createClassLogger(o.getLogger(), LoggingLevelType.INFO)));
        Arrays.stream(ModelLogger.values()).forEach(o -> list.add(createClassLogger(o.getLogger(), LoggingLevelType.INFO)));

        return list;
    }
}
