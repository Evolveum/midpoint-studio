package com.evolveum.midpoint.studio.impl.log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class LogCaptureWriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path dir() {
        return folder.getRoot().toPath();
    }

    @Test
    public void writesTextToFile() throws Exception {
        File file = dir().resolve("Env.log").toFile();
        try (LogCaptureWriter writer = new LogCaptureWriter(file, 1000, 3)) {
            writer.write("hello\n");
        }

        assertEquals("hello\n", Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    public void rollsOverWhenMaxSizeExceeded() throws Exception {
        File file = dir().resolve("Env.log").toFile();
        try (LogCaptureWriter writer = new LogCaptureWriter(file, 10, 3)) {
            writer.write("0123456789");   // exactly at limit
            writer.write("next");          // must land in a fresh file
        }

        assertEquals("next", Files.readString(dir().resolve("Env.log")));
        assertEquals("0123456789", Files.readString(dir().resolve("Env.log.1")));
    }

    @Test
    public void oldestRolledFileIsDroppedAtMaxFiles() throws Exception {
        File file = dir().resolve("Env.log").toFile();
        try (LogCaptureWriter writer = new LogCaptureWriter(file, 5, 2)) {
            writer.write("first");    // fills Env.log
            writer.write("second");   // roll -> .1=first; fills Env.log
            writer.write("third");    // roll -> .1=second (first dropped, maxFiles=2)
        }

        assertEquals("third", Files.readString(dir().resolve("Env.log")));
        assertEquals("second", Files.readString(dir().resolve("Env.log.1")));
        assertFalse(Files.exists(dir().resolve("Env.log.2")));
    }

    @Test
    public void byteCountUsesUtf8NotCharCount() throws Exception {
        File file = dir().resolve("Env.log").toFile();
        try (LogCaptureWriter writer = new LogCaptureWriter(file, 4, 3)) {
            writer.write("řř");   // 4 bytes in UTF-8, 2 chars - at limit
            writer.write("x");
        }

        assertEquals("x", Files.readString(dir().resolve("Env.log")));
        assertEquals("řř", Files.readString(dir().resolve("Env.log.1")));
    }

    @Test
    public void appendsToExistingFileAndCountsItsSize() throws Exception {
        Files.writeString(dir().resolve("Env.log"), "12345678");   // 8 bytes pre-existing
        File file = dir().resolve("Env.log").toFile();
        try (LogCaptureWriter writer = new LogCaptureWriter(file, 10, 3)) {
            writer.write("90");    // reaches limit of 10
            writer.write("next");  // rolls
        }

        assertEquals("next", Files.readString(dir().resolve("Env.log")));
        assertEquals("1234567890", Files.readString(dir().resolve("Env.log.1")));
    }
}
