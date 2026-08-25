package com.evolveum.midpoint.studio.impl.log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log level of a server log entry, extracted from the entry's first line.
 */
public enum LogLevel {

    ERROR, WARN, INFO, DEBUG, TRACE, UNKNOWN;

    private static final Pattern LEVEL = Pattern.compile("\\b(ERROR|WARN|INFO|DEBUG|TRACE)\\b");

    public static LogLevel fromLine(String line) {
        Matcher matcher = LEVEL.matcher(line);
        if (matcher.find()) {
            return valueOf(matcher.group(1));
        }

        return UNKNOWN;
    }
}
