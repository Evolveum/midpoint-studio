/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.studio.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.evolveum.midpoint.studio.cmd.action.Action;
import com.evolveum.midpoint.studio.cmd.opts.BaseOptions;
import com.evolveum.midpoint.studio.cmd.opts.EnvironmentOptions;
import com.evolveum.midpoint.studio.cmd.util.StudioUtil;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StudioCmdMain {

    public static void main(String[] args) {
        new StudioCmdMain().run(args);
    }

    protected void run(String[] args) {
        JCommander jc = StudioUtil.setupCommandLineParser();

        try {
            jc.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            return;
        }

        String parsedCommand = jc.getParsedCommand();

        BaseOptions base = StudioUtil.getOptions(jc, BaseOptions.class);

        if (base.isVersion()) {
            try {
                Path path = Paths.get(StudioCmdMain.class.getResource("/version").toURI());
                String version = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
                System.out.println(version);
            } catch (Exception ex) {
                System.err.println("Couldn't load version information, reason: " + ex.getMessage());
            }
            return;
        }

        if (base.isHelp() || parsedCommand == null) {
            printHelp(jc, parsedCommand);
            return;
        }

        if (base.isVerbose() && base.isSilent()) {
            System.err.println("Cant' use " + BaseOptions.P_VERBOSE + " and " + BaseOptions.P_SILENT
                    + " together (verbose and silent)");
            printHelp(jc, parsedCommand);
            return;
        }

        StudioContext context = null;
        try {
            EnvironmentOptions environment = StudioUtil.getOptions(jc, EnvironmentOptions.class);
            Action action = Command.createAction(parsedCommand);

            if (action == null) {
                System.err.println("Action for command '" + parsedCommand + "' not found");
                return;
            }

            Object options = jc.getCommands().get(parsedCommand).getObjects().get(0);

            context = new StudioContext(jc);
            context.init();

            preInit(context);

            action.init(context, options);

            preExecute(context);

            action.execute();

            postExecute(context);
        } catch (Exception ex) {
            handleException(base, ex);
        } finally {
            cleanupResources(base, context);
        }
    }

    protected void preInit(StudioContext context) {
        // intentionally left out empty
    }

    protected void preExecute(StudioContext context) {
        // intentionally left out empty
    }

    protected void postExecute(StudioContext context) {
        // intentionally left out empty
    }

    private void cleanupResources(BaseOptions opts, StudioContext context) {
        try {
            if (context != null) {
                context.destroy();
            }
        } catch (Exception ex) {
            if (opts.isVerbose()) {
                String stack = StudioUtil.printStackToString(ex);

                System.err.print("Unexpected exception occurred (" + ex.getClass()
                        + ") during destroying context. Exception stack trace:\n" + stack);
            }
        }
    }

    private void handleException(BaseOptions opts, Exception ex) {
        if (!opts.isSilent()) {
            System.err.println("Unexpected exception occurred (" + ex.getClass() + "), reason: " + ex.getMessage());
        }

        if (opts.isVerbose()) {
            String stack = StudioUtil.printStackToString(ex);

            System.err.print("Exception stack trace:\n" + stack);
        }
    }

    private void printHelp(JCommander jc, String parsedCommand) {
        jc.usage();
    }
}
