package com.evolveum.midpoint.studio.client;

/**
 * One fragment of the server log file, as returned by GET /ws/rest/log.
 * <p>
 * EXPERIMENTAL: {@link #getContent()} returns raw bytes, not a decoded string.
 * The server slices the log file at arbitrary byte offsets, so a multi-byte UTF-8
 * character can be split across two fragments. Decoding must happen only after the
 * caller has re-joined fragments on line boundaries ('\n' cannot occur inside a
 * multi-byte UTF-8 sequence). Decoding this fragment directly will corrupt such
 * characters to U+FFFD.
 */
public class LogFileContent {

    private final byte[] content;

    private final long at;

    private final boolean complete;

    private final long logFileSize;

    public LogFileContent(byte[] content, long at, boolean complete, long logFileSize) {
        this.content = content != null ? content : new byte[0];
        this.at = at;
        this.complete = complete;
        this.logFileSize = logFileSize;
    }

    public byte[] getContent() {
        return content;
    }

    /**
     * Byte offset the server actually started reading at (header ReturnedDataPosition).
     */
    public long getAt() {
        return at;
    }

    /**
     * True if the server read to EOF, false if truncated by maxSize (header ReturnedDataComplete).
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Log file length at read time (header CurrentLogFileSize).
     */
    public long getLogFileSize() {
        return logFileSize;
    }
}
