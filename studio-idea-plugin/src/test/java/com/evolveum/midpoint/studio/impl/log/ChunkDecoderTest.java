package com.evolveum.midpoint.studio.impl.log;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ChunkDecoderTest {

    private final ChunkDecoder decoder = new ChunkDecoder();

    @Test
    public void completeLinesAreDecoded() {
        String text = decoder.feed("line1\nline2\n".getBytes(StandardCharsets.UTF_8));

        assertEquals("line1\nline2\n", text);
    }

    @Test
    public void trailingPartialLineIsHeldUntilCompleted() {
        String first = decoder.feed("line1\npart".getBytes(StandardCharsets.UTF_8));
        String second = decoder.feed("ial\n".getBytes(StandardCharsets.UTF_8));

        assertEquals("line1\n", first);
        assertEquals("partial\n", second);
    }

    @Test
    public void chunkWithNoNewlineYieldsNull() {
        assertNull(decoder.feed("no newline here".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void multiByteCharacterSplitAcrossChunksSurvives() {
        byte[] whole = "před\n".getBytes(StandardCharsets.UTF_8);   // 'ř' is 2 bytes
        // split inside the 'ř' sequence: 'p' is 1 byte, 'ř' starts at index 1
        byte[] a = Arrays.copyOfRange(whole, 0, 2);   // cuts 'ř' in half
        byte[] b = Arrays.copyOfRange(whole, 2, whole.length);

        assertNull(decoder.feed(a));
        assertEquals("před\n", decoder.feed(b));
    }

    @Test
    public void flushReturnsPendingBytesAsFinalText() {
        decoder.feed("incomplete".getBytes(StandardCharsets.UTF_8));

        assertEquals("incomplete", decoder.flush());
        assertNull(decoder.flush());
    }

    @Test
    public void resetDiscardsPending() {
        decoder.feed("incomplete".getBytes(StandardCharsets.UTF_8));
        decoder.reset();

        assertNull(decoder.flush());
    }
}
