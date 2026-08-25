package com.evolveum.midpoint.studio.impl.log;

import org.junit.Test;

import static org.junit.Assert.*;

public class LogEntryParserTest {

    private static final String TS = "2026-08-18 10:15:30,123";

    private final LogEntryParser parser = new LogEntryParser();

    @Test
    public void timestampLineStartsNewEntry() {
        LogEntryParser.ParsedLine first = parser.feedLine(TS + " [main] INFO (c.e.m.Foo): hello");
        LogEntryParser.ParsedLine second = parser.feedLine(TS + " [main] ERROR (c.e.m.Bar): boom");

        assertTrue(first.newEntry());
        assertTrue(second.newEntry());
        assertNotSame(first.entry(), second.entry());
        assertEquals(LogLevel.INFO, first.entry().getLevel());
        assertEquals(LogLevel.ERROR, second.entry().getLevel());
    }

    @Test
    public void continuationLinesAttachToCurrentEntry() {
        LogEntryParser.ParsedLine first = parser.feedLine(TS + " [main] ERROR (c.e.m.Bar): boom");
        LogEntryParser.ParsedLine trace = parser.feedLine("java.lang.IllegalStateException: kaboom");
        LogEntryParser.ParsedLine frame = parser.feedLine("\tat com.example.Foo.bar(Foo.java:10)");

        assertFalse(trace.newEntry());
        assertFalse(frame.newEntry());
        assertSame(first.entry(), trace.entry());
        assertSame(first.entry(), frame.entry());
        assertEquals(TS + " [main] ERROR (c.e.m.Bar): boom\n"
                + "java.lang.IllegalStateException: kaboom\n"
                + "\tat com.example.Foo.bar(Foo.java:10)\n", first.entry().getText());
    }

    @Test
    public void leadingFragmentBecomesUnknownLevelEntry() {
        // tail seeding usually cuts into the middle of an entry
        LogEntryParser.ParsedLine fragment = parser.feedLine("\tat com.example.Foo.bar(Foo.java:10)");

        assertTrue(fragment.newEntry());
        assertEquals(LogLevel.UNKNOWN, fragment.entry().getLevel());
    }

    @Test
    public void isoTimestampWithTAndDotAlsoStartsEntry() {
        LogEntryParser.ParsedLine line = parser.feedLine("2026-08-18T10:15:30.123 WARN something");

        assertTrue(line.newEntry());
        assertEquals(LogLevel.WARN, line.entry().getLevel());
    }

    @Test
    public void unparseableFormatDegradesToGroupingWithoutDroppingLines() {
        LogEntryParser.ParsedLine a = parser.feedLine("some custom format");
        LogEntryParser.ParsedLine b = parser.feedLine("another line");

        // grouping degrades (both under one UNKNOWN entry), content is preserved
        assertTrue(a.newEntry());
        assertFalse(b.newEntry());
        assertEquals("some custom format\nanother line\n", a.entry().getText());
    }

    @Test
    public void levelTokenInMessageBodyDoesNotOverrideFirstToken() {
        LogEntryParser.ParsedLine line = parser.feedLine(TS + " [main] INFO (c.e.m.Foo): user typed ERROR");

        assertEquals(LogLevel.INFO, line.entry().getLevel());
    }

    @Test
    public void entryIsForceSplitAfterContinuationCap() {
        LogEntryParser.ParsedLine first = parser.feedLine("unrecognized format line 0");
        LogEntryParser.ParsedLine last = null;
        for (int i = 1; i <= LogEntryParser.MAX_CONTINUATION_LINES; i++) {
            last = parser.feedLine("unrecognized format line " + i);
        }

        // all lines up to the cap belong to the first entry
        assertNotNull(last);
        assertFalse(last.newEntry());
        assertSame(first.entry(), last.entry());

        // the line after the cap starts a fresh entry - one session can't become one entry
        LogEntryParser.ParsedLine overflow = parser.feedLine("unrecognized format overflow");
        assertTrue(overflow.newEntry());
        assertNotSame(first.entry(), overflow.entry());
    }

    @Test
    public void matchingLineResetsContinuationCap() {
        for (int i = 0; i < LogEntryParser.MAX_CONTINUATION_LINES - 1; i++) {
            parser.feedLine("continuation " + i);
        }
        LogEntryParser.ParsedLine entry = parser.feedLine(TS + " INFO fresh entry");
        LogEntryParser.ParsedLine continuation = parser.feedLine("its stack frame");

        assertTrue(entry.newEntry());
        assertFalse(continuation.newEntry());
        assertSame(entry.entry(), continuation.entry());
    }

    @Test
    public void blankPatternMakesEveryLineItsOwnEntry() {
        LogEntryParser lineParser = new LogEntryParser("");

        LogEntryParser.ParsedLine a = lineParser.feedLine("anything");
        LogEntryParser.ParsedLine b = lineParser.feedLine("\tat com.example.Foo.bar(Foo.java:10)");

        assertTrue(a.newEntry());
        assertTrue(b.newEntry());
        assertNotSame(a.entry(), b.entry());
    }

    @Test
    public void customPatternMatchesTimestampAnywhereInLine() {
        // level-first format: "INFO 2026-08-18 10:15:30,123 ..." - unanchored date regex
        LogEntryParser custom = new LogEntryParser("\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}[.,]\\d{3}");

        LogEntryParser.ParsedLine entry = custom.feedLine("INFO " + TS + " (c.e.m.Foo): hello");
        LogEntryParser.ParsedLine continuation = custom.feedLine("no timestamp here");

        assertTrue(entry.newEntry());
        assertEquals(LogLevel.INFO, entry.entry().getLevel());
        assertFalse(continuation.newEntry());
    }
}
