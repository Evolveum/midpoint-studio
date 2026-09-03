package com.evolveum.midpoint.studio.impl.log;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class LogEntryBufferTest {

    private static LogEntry entry(String text) {
        LogEntry e = new LogEntry(LogLevel.INFO);
        e.appendLine(text);
        return e;
    }

    @Test
    public void evictsOldestBeyondCapacity() {
        LogEntryBuffer buffer = new LogEntryBuffer(3);
        buffer.add(entry("a"));
        buffer.add(entry("b"));
        buffer.add(entry("c"));
        buffer.add(entry("d"));

        List<LogEntry> snapshot = buffer.snapshot();
        assertEquals(3, snapshot.size());
        assertEquals("b\n", snapshot.get(0).getText());
        assertEquals("d\n", snapshot.get(2).getText());
    }

    @Test
    public void clearEmpties() {
        LogEntryBuffer buffer = new LogEntryBuffer(3);
        buffer.add(entry("a"));
        buffer.clear();

        assertTrue(buffer.snapshot().isEmpty());
    }

    @Test
    public void snapshotIsIndependentOfLaterMutation() {
        LogEntryBuffer buffer = new LogEntryBuffer(3);
        buffer.add(entry("a"));
        List<LogEntry> snapshot = buffer.snapshot();
        buffer.clear();

        assertEquals(1, snapshot.size());
    }
}
