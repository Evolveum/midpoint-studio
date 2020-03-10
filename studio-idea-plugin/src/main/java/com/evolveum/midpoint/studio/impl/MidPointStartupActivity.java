package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.client.impl.ServiceFactory;
import com.evolveum.midpoint.prism.PrismContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointStartupActivity implements StartupActivity {

    private static final Logger LOG = Logger.getInstance(MidPointStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("Initializing service factory");
        PrismContext ctx = ServiceFactory.DEFAULT_PRISM_CONTEXT;
        LOG.info("Service factory initialized " + ctx.toString());
    }

}
