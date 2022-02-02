package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midscribe.generator.LogListener;
import com.intellij.openapi.diagnostic.Logger;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidscribeLogListener implements LogListener {

    private static final Logger LOG = Logger.getInstance(MidscribeLogListener.class);

    private Environment environment;

    private MidPointService midPointService;

    public MidscribeLogListener(Environment environment, MidPointService midPointService) {
        this.environment = environment;
        this.midPointService = midPointService;
    }

    public void log(Level level, String message, MessageDetails details) {
        String msg = level.name() + ": " + details + ": " + message;

        LOG.info(msg);

        if (midPointService != null) {
            midPointService.printToConsole(environment, MidscribeLogListener.class, message);
        }
    }
}
