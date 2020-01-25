package com.evolveum.midpoint.philosopher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.apache.commons.io.IOUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PhilosopherMain {

    public static void main(String[] args) {
        new PhilosopherMain().run(args);
    }

    private void run(String[] args) {
        JCommander jc = setupCommandLineParser();

        try {
            jc.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            return;
        }

        String parsedCommand = jc.getParsedCommand();

        BaseOptions base = getOptions(jc, BaseOptions.class);

        if (base.isVersion()) {
            try {
                String version = IOUtils.toString(PhilosopherMain.class.getResource("/version"), StandardCharsets.UTF_8);
                System.out.println(version);
            } catch (Exception ex) {
                handleException(base, ex);
            }
            return;
        }

        if (base.isHelp() || parsedCommand == null) {
            printHelp(jc, parsedCommand);
            return;
        }

        try {
            Action action = Command.createAction(parsedCommand);

            if (action == null) {
                System.err.println("Action for command '" + parsedCommand + "' not found");
                return;
            }

            Object options = jc.getCommands().get(parsedCommand).getObjects().get(0);
            action.init(options);
            action.execute();
        } catch (Exception ex) {
            handleException(base, ex);
        }
    }

    private void handleException(BaseOptions opts, Exception ex) {
        if (!opts.isSilent()) {
            System.err.println("Unexpected exception occurred (" + ex.getClass() + "), reason: " + ex.getMessage());
        }

        if (opts.isVerbose()) {
            String stack = printStackToString(ex);

            System.err.print("Exception stack trace:\n" + stack);
        }
    }

    private String printStackToString(Exception ex) {
        if (ex == null) {
            return null;
        }

        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));

        return writer.toString();
    }

    private <T> T getOptions(JCommander jc, Class<T> type) {
        List<Object> objects = jc.getObjects();
        for (Object object : objects) {
            if (type.equals(object.getClass())) {
                return (T) object;
            }
        }

        return null;
    }

    private void printHelp(JCommander jc, String parsedCommand) {
        if (parsedCommand == null) {
            jc.usage();
        } else {
            jc.usage(parsedCommand);
        }
    }

    private JCommander setupCommandLineParser() {
        BaseOptions base = new BaseOptions();

        JCommander.Builder builder = JCommander.newBuilder()
                .expandAtSign(false)
                .addObject(base);

        for (Command cmd : Command.values()) {
            builder.addCommand(cmd.getCommandName(), cmd.createOptions());
        }

        JCommander jc = builder.build();
        jc.setProgramName("java [-Dlogback.configurationFile=logback.xml] -jar philosopher.jar");
        jc.setColumnSize(150);

        return jc;
    }
}
