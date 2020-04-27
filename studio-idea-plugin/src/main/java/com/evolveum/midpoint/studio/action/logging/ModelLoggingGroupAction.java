package com.evolveum.midpoint.studio.action.logging;

import com.intellij.openapi.actionSystem.Separator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ModelLoggingGroupAction extends SetModuleLoggingGroupAction {

    public ModelLoggingGroupAction() {
        super("com.evolveum.midpoint.model");

        add(new Separator());

        // todo implement
//        addAction(new SetLoggerAction());
//
//        MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_CLOCKWORK_SUMMARY, "Set to 'clockwork summary' (Clockwork=DEBUG)");
//        MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_PROJECTOR_SUMMARY, "Set to 'projector summary' (previous + Projector=TRACE)");
//        MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_MAPPING_TRACE, "Set to 'mapping trace' (previous + Mapping=TRACE)");
//        MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_EXPRESSION_TRACE, "Set to 'expression trace' (previous + Expression=TRACE)");
//        MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_PROJECTOR_TRACE, "Set to 'projector trace' (previous + projector.*=TRACE)");
//        MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_LENS_TRACE, "Set to 'lens trace' (previous + lens.*=TRACE)");
//        MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_DEBUG, "Set to DEBUG (whole module)");
//        MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_TRACE, "Set to TRACE (whole module)");
    }
}
