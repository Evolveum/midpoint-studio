package com.evolveum.midpoint.studio.impl.log;

import java.util.regex.Pattern;

/**
 * Groups physical log lines into {@link LogEntry} objects. A line matching the
 * entry-start pattern begins a new entry, anything else continues the current one.
 * <p>
 * Two guards against log formats the pattern doesn't recognize:
 * lines are never dropped (an unmatched leading line starts an UNKNOWN entry), and
 * an entry is force-split after {@link #MAX_CONTINUATION_LINES} continuations so a
 * format with no matching lines at all cannot accumulate the whole session into one
 * ever-growing entry.
 */
public class LogEntryParser {

    public static final String DEFAULT_ENTRY_START_PATTERN =
            "^\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}[.,]\\d{3}";

    /**
     * Real multi-line entries (stack traces, XML dumps) run tens to hundreds of lines;
     * anything past this is a format we're not parsing, not one entry.
     */
    static final int MAX_CONTINUATION_LINES = 1000;

    private final Pattern entryStart;

    private LogEntry current;

    private int continuationLines;

    public LogEntryParser() {
        this(DEFAULT_ENTRY_START_PATTERN);
    }

    /**
     * @param entryStartPattern regex marking the first line of an entry, matched anywhere
     *                          in the line (anchor with ^ to require line start). Blank
     *                          means every line is its own entry.
     */
    public LogEntryParser(String entryStartPattern) {
        this.entryStart = entryStartPattern == null || entryStartPattern.isBlank()
                ? null
                : Pattern.compile(entryStartPattern);
    }

    public record ParsedLine(LogEntry entry, String line, boolean newEntry) {
    }

    public ParsedLine feedLine(String line) {
        boolean startsEntry = entryStart == null || entryStart.matcher(line).find();

        if (startsEntry || current == null || continuationLines >= MAX_CONTINUATION_LINES) {
            current = new LogEntry(startsEntry ? LogLevel.fromLine(line) : LogLevel.UNKNOWN);
            current.appendLine(line);
            continuationLines = 0;
            return new ParsedLine(current, line, true);
        }

        current.appendLine(line);
        continuationLines++;
        return new ParsedLine(current, line, false);
    }

    public void reset() {
        current = null;
        continuationLines = 0;
    }
}
