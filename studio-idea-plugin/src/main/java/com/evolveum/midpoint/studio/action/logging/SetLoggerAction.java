package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;

import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetLoggerAction extends BackgroundAction {

    private static final Logger LOG = Logger.getInstance(SetLoggerAction.class);

    public SetLoggerAction() {
        super("Updating logging configuration");
    }

    @Override
    protected void executeOnBackground(AnActionEvent e, ProgressIndicator indicator) {
        MidPointManager mm = MidPointManager.getInstance(e.getProject());
        mm.printToConsole(getClass(), "Initializing action");

        LOG.debug("Setting up MidPoint client");

        EnvironmentManager em = EnvironmentManager.getInstance(e.getProject());
        Environment env = em.getSelected();
        MidPointClient client = new MidPointClient(e.getProject(), env);

        LOG.debug("MidPoint client setup done");

        PrismObject<SystemConfigurationType> configPrism = client.get(SystemConfigurationType.class,
                SystemObjectsType.SYSTEM_CONFIGURATION.value(), new SearchOptions());

        SystemConfigurationType config = configPrism.asObjectable();
        LoggingConfigurationType logging = config.getLogging();

        if (logging == null) {
            logging = new LoggingConfigurationType();
            config.setLogging(logging);
        }


        // todo implement
    }

    public List<ClassLoggerConfigurationType> buildClassLoggers() {
        return Collections.emptyList();
    }

    public List<SubSystemLoggerConfigurationType> buildSubsystemLoggers() {
        return Collections.emptyList();
    }
}
