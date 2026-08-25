package com.evolveum.midpoint.studio.impl.log;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.ClientException;
import com.evolveum.midpoint.studio.client.LogFileContent;

import java.util.function.BooleanSupplier;

/**
 * Poll-loop state machine for tailing the server log over GET /ws/rest/log.
 * Holds the byte offset, detects rotation, classifies errors and computes backoff.
 * Scheduling is the caller's job: {@link #pollOnce()} performs one fetch and returns
 * the delay in millis until the next call should happen (0 = immediately,
 * {@link #STOP} = stop permanently).
 */
public class ServerLogTailer {

    public interface LogSource {

        LogFileContent fetch(long fromPosition, long maxSize) throws Exception;
    }

    public interface Listener {

        void onText(String chunk);

        void onRotation();

        void onFatalError(String message, Exception ex);

        void onTransientError(Exception ex, long retryInMillis);
    }

    public record Config(long pollIntervalMillis, long initialTailSize, long maxChunkSize, long maxBackoffMillis) {
    }

    public static final long STOP = -1;

    private final LogSource source;

    private final Config config;

    private final BooleanSupplier enabled;

    private final Listener listener;

    private final ChunkDecoder decoder = new ChunkDecoder();

    private Long nextPosition;   // null = unseeded

    private long backoffMillis;  // 0 = no active backoff

    public ServerLogTailer(LogSource source, Config config, BooleanSupplier enabled, Listener listener) {
        this.source = source;
        this.config = config;
        this.enabled = enabled;
        this.listener = listener;
    }

    public long pollOnce() {
        if (!enabled.getAsBoolean()) {
            return STOP;
        }

        long from = nextPosition == null ? -config.initialTailSize() : nextPosition;

        LogFileContent content;
        try {
            content = source.fetch(from, config.maxChunkSize());
        } catch (AuthenticationException ex) {
            listener.onFatalError("Authentication failed: " + ex.getMessage(), ex);
            return STOP;
        } catch (ClientException ex) {
            // server responded with an error: 403, no log file configured, ...
            listener.onFatalError(ex.getMessage(), ex);
            return STOP;
        } catch (Exception ex) {
            backoffMillis = backoffMillis == 0
                    ? config.pollIntervalMillis()
                    : Math.min(backoffMillis * 2, config.maxBackoffMillis());
            listener.onTransientError(ex, backoffMillis);
            return backoffMillis;
        }

        backoffMillis = 0;

        if (nextPosition != null && content.getLogFileSize() >= 0
                && content.getLogFileSize() < nextPosition) {
            // file rotated or truncated under us; emit whatever line fragment was still
            // held back before reseeding with a tail of the new file
            flush();
            nextPosition = null;
            listener.onRotation();
            return 0;
        }

        String text = decoder.feed(content.getContent());
        if (text != null) {
            listener.onText(text);
        }

        if (content.isComplete()) {
            nextPosition = content.getLogFileSize();
            return config.pollIntervalMillis();
        }

        nextPosition = content.getAt() + config.maxChunkSize();
        return 0;
    }

    /**
     * Emits any held-back partial line, newline-terminated so line-oriented consumers
     * don't glue subsequent output onto it. Called on stop and on rotation.
     */
    public void flush() {
        String rest = decoder.flush();
        if (rest != null) {
            listener.onText(rest.endsWith("\n") ? rest : rest + "\n");
        }
    }
}
