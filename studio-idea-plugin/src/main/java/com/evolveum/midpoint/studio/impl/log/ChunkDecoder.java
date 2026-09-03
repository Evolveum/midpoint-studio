package com.evolveum.midpoint.studio.impl.log;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Assembles raw byte chunks (sliced by the server at arbitrary byte offsets) into
 * decodable UTF-8 text. Splits on '\n' bytes - which cannot occur inside a multi-byte
 * UTF-8 sequence - and carries the trailing partial line as bytes, so characters cut
 * in half by a chunk boundary are reassembled instead of decoded to U+FFFD.
 */
public class ChunkDecoder {

    private ByteArrayOutputStream pending = new ByteArrayOutputStream();

    /**
     * Returns decoded text up to and including the last complete line, or null if
     * no complete line is available yet.
     */
    public String feed(byte[] chunk) {
        pending.writeBytes(chunk);

        byte[] all = pending.toByteArray();
        int lastNewline = -1;
        for (int i = all.length - 1; i >= 0; i--) {
            if (all[i] == '\n') {
                lastNewline = i;
                break;
            }
        }

        if (lastNewline < 0) {
            return null;
        }

        String text = new String(all, 0, lastNewline + 1, StandardCharsets.UTF_8);

        pending = new ByteArrayOutputStream();
        pending.write(all, lastNewline + 1, all.length - lastNewline - 1);

        return text;
    }

    /**
     * Returns whatever partial line is pending, decoded, or null. Used when the tail
     * stops or the file rotates, so held-back content is not lost.
     */
    public String flush() {
        if (pending.size() == 0) {
            return null;
        }

        String text = pending.toString(StandardCharsets.UTF_8);
        pending = new ByteArrayOutputStream();

        return text;
    }

    public void reset() {
        pending = new ByteArrayOutputStream();
    }
}
