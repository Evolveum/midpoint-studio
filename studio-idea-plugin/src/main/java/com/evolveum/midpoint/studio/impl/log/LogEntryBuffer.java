package com.evolveum.midpoint.studio.impl.log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Bounded ring buffer of log entries - the single source of truth the view
 * re-renders from when the filter changes. Thread-safe: written from the poll
 * thread, snapshotted from the EDT.
 */
public class LogEntryBuffer {

    private final int maxEntries;

    private final Deque<LogEntry> entries = new ArrayDeque<>();

    public LogEntryBuffer(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public synchronized void add(LogEntry entry) {
        entries.addLast(entry);
        while (entries.size() > maxEntries) {
            entries.removeFirst();
        }
    }

    public synchronized List<LogEntry> snapshot() {
        return new ArrayList<>(entries);
    }

    public synchronized void clear() {
        entries.clear();
    }
}
