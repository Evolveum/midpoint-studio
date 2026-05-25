package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * IntelliJ-facing lexer wrapper over ANTLR lexer.
 * It preserves token continuity for editor/highlighter/PSI consumers.
 */
public class MelLexerAdaptor extends LexerBase {

    static {
        MelUtils.initialize();
    }

    private final MELLexer antlrLexer = new MELLexer(null);

    private CharSequence buffer = "";
    private int bufferStart;
    private int bufferEnd;

    private IElementType tokenType;
    private int tokenStart;
    private int tokenEnd;

    private int nextOffset;
    private Token pendingAntlrToken;

    public static MelLexerAdaptor newInstance() {
        return new MelLexerAdaptor();
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferStart = startOffset;
        this.bufferEnd = endOffset;

        this.tokenType = null;
        this.tokenStart = startOffset;
        this.tokenEnd = startOffset;

        this.nextOffset = startOffset;
        this.pendingAntlrToken = null;

        String slice = buffer.subSequence(startOffset, endOffset).toString();
        antlrLexer.setInputStream(CharStreams.fromString(slice));

        locateNextToken();
    }

    @Override
    public int getState() {
        // CEL lexer is single-mode for now; no incremental state tracking needed.
        return 0;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @Override
    public void advance() {
        if (tokenType == null) {
            return;
        }
        nextOffset = tokenEnd;
        locateNextToken();
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    private void locateNextToken() {
        // If we previously found a token after a gap, emit it now.
        if (pendingAntlrToken != null) {
            Token t = pendingAntlrToken;
            pendingAntlrToken = null;
            emitAntlrToken(t);
            return;
        }

        if (nextOffset >= bufferEnd) {
            clearToken();
            return;
        }

        while (true) {
            Token t = antlrLexer.nextToken();
            if (t == null || t.getType() == Token.EOF) {
                // Ensure trailing uncovered text is still tokenized.
                if (nextOffset < bufferEnd) {
                    emitSynthetic(MelTokenTypes.BAD_TOKEN_TYPE, nextOffset, bufferEnd);
                } else {
                    clearToken();
                }
                return;
            }

            int absStart = bufferStart + t.getStartIndex();
            int absEnd = bufferStart + t.getStopIndex() + 1;

            // Guard against malformed/empty/overlapping token ranges.
            if (absEnd <= absStart) {
                continue;
            }
            if (absEnd <= nextOffset) {
                continue;
            }
            if (absStart < nextOffset) {
                absStart = nextOffset;
            }

            // Fill gaps so IntelliJ never sees discontinuous token stream.
            if (absStart > nextOffset) {
                pendingAntlrToken = t;
                emitSynthetic(MelTokenTypes.BAD_TOKEN_TYPE, nextOffset, absStart);
                return;
            }

            emitMapped(t, absStart, absEnd);
            return;
        }
    }

    private void emitAntlrToken(Token t) {
        int absStart = bufferStart + t.getStartIndex();
        int absEnd = bufferStart + t.getStopIndex() + 1;
        if (absEnd <= absStart) {
            locateNextToken();
            return;
        }
        if (absStart < nextOffset) {
            absStart = nextOffset;
        }
        emitMapped(t, absStart, absEnd);
    }

    private void emitMapped(Token t, int absStart, int absEnd) {
        IElementType mapped = MelTokenTypes.BAD_TOKEN_TYPE;
        int type = t.getType();
        if (type >= 0 && type < MelTokenTypes.TOKEN_ELEMENT_TYPES.size()) {
            mapped = MelTokenTypes.TOKEN_ELEMENT_TYPES.get(type);
        }
        emitSynthetic(mapped, absStart, Math.min(absEnd, bufferEnd));
    }

    private void emitSynthetic(IElementType type, int start, int end) {
        if (end <= start) {
            clearToken();
            return;
        }

        tokenType = type;
        tokenStart = start;
        tokenEnd = end;
    }

    private void clearToken() {
        tokenType = null;
        tokenStart = bufferEnd;
        tokenEnd = bufferEnd;
    }
}
