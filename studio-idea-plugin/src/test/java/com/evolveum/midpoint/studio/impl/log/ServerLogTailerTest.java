package com.evolveum.midpoint.studio.impl.log;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.LogFileContent;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.Assert.*;

public class ServerLogTailerTest {

    // pollIntervalMillis, initialTailSize, maxChunkSize, maxBackoffMillis
    private static final ServerLogTailer.Config CONFIG =
            new ServerLogTailer.Config(3000, 64, 1024, 60_000);

    private static class FakeSource implements ServerLogTailer.LogSource {
        final Deque<Object> responses = new ArrayDeque<>();   // LogFileContent or Exception
        final List<Long> requestedFrom = new ArrayList<>();
        final List<Long> requestedMax = new ArrayList<>();

        void enqueue(String text, long at, boolean complete, long fileSize) {
            responses.add(new LogFileContent(
                    text.getBytes(StandardCharsets.UTF_8), at, complete, fileSize));
        }

        void enqueue(Exception ex) {
            responses.add(ex);
        }

        @Override
        public LogFileContent fetch(long fromPosition, long maxSize) throws Exception {
            requestedFrom.add(fromPosition);
            requestedMax.add(maxSize);
            Object next = responses.remove();
            if (next instanceof Exception ex) {
                throw ex;
            }
            return (LogFileContent) next;
        }
    }

    private static class RecordingListener implements ServerLogTailer.Listener {
        final StringBuilder text = new StringBuilder();
        int rotations;
        final List<String> fatals = new ArrayList<>();
        final List<Long> transientRetries = new ArrayList<>();

        @Override
        public void onText(String chunk) {
            text.append(chunk);
        }

        @Override
        public void onRotation() {
            rotations++;
        }

        @Override
        public void onFatalError(String message, Exception ex) {
            fatals.add(message);
        }

        @Override
        public void onTransientError(Exception ex, long retryInMillis) {
            transientRetries.add(retryInMillis);
        }
    }

    private final FakeSource source = new FakeSource();
    private final RecordingListener listener = new RecordingListener();

    private ServerLogTailer tailer(boolean enabled) {
        return new ServerLogTailer(source, CONFIG, () -> enabled, listener);
    }

    @Test
    public void firstPollSeedsWithNegativeTail() {
        source.enqueue("a\n", 936, true, 1000);
        ServerLogTailer tailer = tailer(true);

        long delay = tailer.pollOnce();

        assertEquals(-64L, (long) source.requestedFrom.get(0));
        assertEquals(1024L, (long) source.requestedMax.get(0));
        assertEquals("a\n", listener.text.toString());
        assertEquals(3000, delay);
    }

    @Test
    public void completeResponseAdvancesToFileSize() {
        source.enqueue("a\n", 936, true, 1000);
        source.enqueue("b\n", 1000, true, 1002);
        ServerLogTailer tailer = tailer(true);

        tailer.pollOnce();
        tailer.pollOnce();

        assertEquals(1000L, (long) source.requestedFrom.get(1));
    }

    @Test
    public void incompleteResponseAdvancesByMaxSizeAndRepollsImmediately() {
        source.enqueue("x\n", 936, false, 100_000);
        ServerLogTailer tailer = tailer(true);

        long delay = tailer.pollOnce();

        assertEquals(0, delay);
        source.enqueue("y\n", 1960, true, 100_000);
        tailer.pollOnce();
        // next offset = at + maxSize = 936 + 1024
        assertEquals(1960L, (long) source.requestedFrom.get(1));
    }

    @Test
    public void rotationDetectedWhenFileShrinks() {
        source.enqueue("a\n", 4936, true, 5000);
        // rotated: file now 10 bytes, position 5000 is past EOF
        source.enqueue("", 5000, true, 10);
        source.enqueue("fresh\n", 0, true, 10);
        ServerLogTailer tailer = tailer(true);

        tailer.pollOnce();
        long delay = tailer.pollOnce();

        assertEquals(1, listener.rotations);
        assertEquals(0, delay);   // reseed immediately

        tailer.pollOnce();
        assertEquals(-64L, (long) source.requestedFrom.get(2));   // reseeded with tail
        assertTrue(listener.text.toString().endsWith("fresh\n"));
    }

    @Test
    public void partialLineIsFlushedOnRotationNotDropped() {
        // chunk ends mid-line: "partial" is held back by the decoder
        source.enqueue("done\npartial", 936, true, 1000);
        // rotation detected: file shrank under our offset
        source.enqueue("", 1000, true, 10);
        source.enqueue("fresh\n", 0, true, 10);
        ServerLogTailer tailer = tailer(true);

        tailer.pollOnce();
        tailer.pollOnce();
        tailer.pollOnce();

        // the fragment is emitted (newline-terminated), then post-rotation text follows
        assertEquals("done\npartial\nfresh\n", listener.text.toString());
        assertEquals(1, listener.rotations);
    }

    @Test
    public void flushOnStopTerminatesFragmentWithNewline() {
        source.enqueue("no newline here", 936, true, 1000);
        ServerLogTailer tailer = tailer(true);

        tailer.pollOnce();
        tailer.flush();

        assertEquals("no newline here\n", listener.text.toString());
    }

    @Test
    public void transientErrorBacksOffExponentiallyAndRecovers() {
        source.enqueue(new IOException("connection refused"));
        source.enqueue(new IOException("connection refused"));
        source.enqueue("ok\n", 936, true, 1000);
        ServerLogTailer tailer = tailer(true);

        long first = tailer.pollOnce();
        long second = tailer.pollOnce();
        long third = tailer.pollOnce();

        assertEquals(3000, first);     // starts at poll interval
        assertEquals(6000, second);    // doubles
        assertEquals(3000, third);     // success resets to poll interval
        assertEquals(2, listener.transientRetries.size());
        assertEquals("ok\n", listener.text.toString());
    }

    @Test
    public void backoffIsCappedAtMaxBackoff() {
        for (int i = 0; i < 10; i++) {
            source.enqueue(new IOException("down"));
        }
        ServerLogTailer tailer = tailer(true);

        long last = 0;
        for (int i = 0; i < 10; i++) {
            last = tailer.pollOnce();
        }

        assertEquals(60_000, last);
    }

    @Test
    public void authenticationErrorIsFatal() {
        source.enqueue(new AuthenticationException("bad credentials"));
        ServerLogTailer tailer = tailer(true);

        long delay = tailer.pollOnce();

        assertEquals(-1, delay);
        assertEquals(1, listener.fatals.size());
    }

    @Test
    public void disabledFlagStopsWithoutFetching() {
        ServerLogTailer tailer = tailer(false);

        long delay = tailer.pollOnce();

        assertEquals(-1, delay);
        assertTrue(source.requestedFrom.isEmpty());
    }
}
