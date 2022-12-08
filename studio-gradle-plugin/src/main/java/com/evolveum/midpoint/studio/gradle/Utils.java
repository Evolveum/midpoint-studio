package com.evolveum.midpoint.studio.gradle;

import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Utils {

    private static final Logger LOG = Logging.getLogger(MidpointStudioPlugin.class);

    public static void error(String logCategory, String message) {
        log(LogLevel.ERROR, logCategory, message, null);
    }

    public static void error(String logCategory, String message, Throwable t) {
        log(LogLevel.ERROR, logCategory, message, t);
    }

    public static void warn(String logCategory, String message) {
        log(LogLevel.WARN, logCategory, message, null);
    }

    public static void warn(String logCategory, String message, Throwable t) {
        log(LogLevel.WARN, logCategory, message, t);
    }

    public static void info(String logCategory, String message) {
        log(LogLevel.INFO, logCategory, message, null);
    }

    public static void info(String logCategory, String message, Throwable t) {
        log(LogLevel.INFO, logCategory, message, t);
    }

    public static void debug(String logCategory, String message) {
        log(LogLevel.DEBUG, logCategory, message, null);
    }

    public static void debug(String logCategory, String message, Throwable t) {
        log(LogLevel.DEBUG, logCategory, message, t);
    }

    private static void log(LogLevel level, String context, String message, Throwable t) {
        String prefix = "[" + Constants.LOG_CATEGORY_PREFIX + "]";
        if (context != null && !context.isBlank()) {
            prefix += " [" + context + "]";
        }
        prefix += " ";

        if (LogLevel.ERROR != level && !LOG.isDebugEnabled() && t != null) {
            LOG.log(level, prefix + message + ". Run with --debug option to get more log output.");
            return;
        }

        LOG.log(level, prefix + message, t);
    }

    public static String getContext(Project project) {
        String path = project.getPath();
        String name = project.getName();

        StringBuilder sb = new StringBuilder();
        sb.append(path);
        if (!Objects.equals(path, name)) {
            sb.append(name);
        }

        return sb.toString();
    }
}
