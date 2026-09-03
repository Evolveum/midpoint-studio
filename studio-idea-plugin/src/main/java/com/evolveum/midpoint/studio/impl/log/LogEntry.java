package com.evolveum.midpoint.studio.impl.log;

/**
 * One log entry: a first (timestamped) line plus any continuation lines
 * (stack trace frames, wrapped XML dumps, ...).
 */
public class LogEntry {

    private final LogLevel level;

    private final StringBuilder text = new StringBuilder();

    private long printedGeneration = -1;

    public LogEntry(LogLevel level) {
        this.level = level;
    }

    public void appendLine(String line) {
        text.append(line).append('\n');
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getText() {
        return text.toString();
    }

    public boolean matches(String filterLowerCase) {
        if (filterLowerCase == null || filterLowerCase.isEmpty()) {
            return true;
        }

        return text.toString().toLowerCase().contains(filterLowerCase);
    }

    /**
     * View bookkeeping: which filter generation this entry was last fully printed in.
     * Lets the live-append path print a previously hidden entry in full the moment
     * a continuation line makes it match the filter.
     */
    public long getPrintedGeneration() {
        return printedGeneration;
    }

    public void setPrintedGeneration(long printedGeneration) {
        this.printedGeneration = printedGeneration;
    }
}
