package com.evolveum.midpoint.studio.impl.log;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Appends captured log text to a file, rolling over to numbered siblings
 * (file.log -> file.log.1 -> file.log.2 ...) when the size limit is reached, so a
 * capture left running indefinitely is bounded at maxFileSize * maxFiles bytes.
 * Pure java.io - VFS refresh is the caller's concern.
 */
public class LogCaptureWriter implements Closeable {

    private final File file;

    private final long maxFileSize;

    private final int maxFiles;

    private OutputStream out;

    private long written;

    public LogCaptureWriter(File file, long maxFileSize, int maxFiles) throws IOException {
        this.file = file;
        this.maxFileSize = maxFileSize;
        this.maxFiles = maxFiles;

        open();
    }

    private void open() throws IOException {
        written = file.exists() ? file.length() : 0;
        out = new BufferedOutputStream(new FileOutputStream(file, true));
    }

    public synchronized void write(String text) throws IOException {
        if (written >= maxFileSize) {
            roll();
        }

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        out.write(bytes);
        out.flush();
        written += bytes.length;
    }

    private void roll() throws IOException {
        out.close();

        File oldest = sibling(maxFiles - 1);
        if (oldest.exists() && !oldest.delete()) {
            throw new IOException("Couldn't delete " + oldest);
        }
        for (int i = maxFiles - 2; i >= 1; i--) {
            File from = sibling(i);
            if (from.exists() && !from.renameTo(sibling(i + 1))) {
                throw new IOException("Couldn't rotate " + from);
            }
        }
        if (!file.renameTo(sibling(1))) {
            throw new IOException("Couldn't rotate " + file);
        }

        open();
    }

    private File sibling(int index) {
        return new File(file.getParentFile(), file.getName() + "." + index);
    }

    @Override
    public synchronized void close() throws IOException {
        out.close();
    }
}
