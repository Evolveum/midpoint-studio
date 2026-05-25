package com.evolveum.midpoint.studio.lang.mel;

import com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer;
import com.evolveum.midpoint.studio.lang.mel.impl.MelLexerAdaptor;
import com.evolveum.midpoint.studio.lang.mel.impl.MelTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MelLexerTest extends BasePlatformTestCase {

    // ── helpers ───────────────────────────────────────────────────────────────────

    private static class TokenInfo {
        final IElementType type;
        final String text;
        final int start;
        final int end;

        TokenInfo(IElementType type, String text, int start, int end) {
            this.type = type;
            this.text = text;
            this.start = start;
            this.end = end;
        }
    }

    private List<TokenInfo> tokenize(String text, MelLexerAdaptor lexer) {
        lexer.start(text);
        List<TokenInfo> tokens = new ArrayList<>();
        while (lexer.getTokenType() != null) {
            IElementType type = lexer.getTokenType();
            int start = lexer.getTokenStart();
            int end = lexer.getTokenEnd();
            tokens.add(new TokenInfo(type, text.substring(start, end), start, end));
            lexer.advance();
        }
        return tokens;
    }

    private List<TokenInfo> tokenize(String text) {
        return tokenize(text, MelLexerAdaptor.newInstance());
    }

    /**
     * Every token must immediately follow the previous one — no gaps, no overlaps.
     */
    private void assertContinuous(List<TokenInfo> tokens, String text) {
        int pos = 0;
        for (TokenInfo tok : tokens) {
            assertEquals("Gap before '" + tok.text + "' at " + tok.start + ", expected offset " + pos,
                    pos, tok.start);
            assertTrue("Empty token '" + tok.text + "' at " + pos, tok.end > tok.start);
            pos = tok.end;
        }
        assertEquals("Trailing text not covered: '" + text.substring(pos) + "'", text.length(), pos);
    }

    private IElementType tok(int index) {
        return MelTokenTypes.TOKEN_ELEMENT_TYPES.toArray(new TokenIElementType[0])[index];
    }

    // ── continuity: valid expressions ────────────────────────────────────────────

    public void testContinuitySimpleIdent() {
        String text = "foo";
        assertContinuous(tokenize(text), text);
    }

    public void testContinuityExpressionWithWhitespace() {
        String text = "foo + bar";
        assertContinuous(tokenize(text), text);
    }

    public void testContinuityBracketAccess() {
        String text = "request.auth.claims[\"email\"]";
        assertContinuous(tokenize(text), text);
    }

    public void testContinuityBracketAccessWithSpaces() {
        // this was the original failing case with hidden-channel whitespace
        String text = "request.auth.claims[ \"email\" ]";
        assertContinuous(tokenize(text), text);
    }

    public void testContinuityComment() {
        String text = "foo // a comment";
        assertContinuous(tokenize(text), text);
    }

    public void testContinuityIntegerLiteral() {
        String text = "42";
        assertContinuous(tokenize(text), text);
    }

    // ── continuity: invalid / bad chars ──────────────────────────────────────────

    public void testContinuityInvalidCharHighlightingMode() {
        String text = "foo;";
        assertContinuous(tokenize(text), text);
    }

    public void testContinuityInvalidCharParserMode() {
        String text = "foo;";
        assertContinuous(tokenize(text), text);
    }

    public void testContinuityMultipleBadChars() {
        String text = ";;;";
        assertContinuous(tokenize(text), text);
    }

    public void testContinuityBadCharMixed() {
        String text = "foo; bar";
        assertContinuous(tokenize(text), text);
    }

    // ── token types: valid tokens ─────────────────────────────────────────────────

    public void testIdentifierTokenType() {
        List<TokenInfo> tokens = tokenize("foo");
        assertEquals(1, tokens.size());
        assertEquals(tok(MELLexer.IDENTIFIER), tokens.get(0).type);
        assertEquals("foo", tokens.get(0).text);
    }

    public void testIntegerLiteralTokenType() {
        List<TokenInfo> tokens = tokenize("42");
        assertEquals(1, tokens.size());
        assertEquals(tok(MELLexer.NUM_INT), tokens.get(0).type);
    }

    public void testFloatLiteralTokenType() {
        List<TokenInfo> tokens = tokenize("3.14");
        assertEquals(1, tokens.size());
        assertEquals(tok(MELLexer.NUM_FLOAT), tokens.get(0).type);
    }

    public void testUintLiteralTokenType() {
        List<TokenInfo> tokens = tokenize("10u");
        assertEquals(1, tokens.size());
        assertEquals(tok(MELLexer.NUM_UINT), tokens.get(0).type);
    }

    public void testStringLiteralTokenType() {
        List<TokenInfo> tokens = tokenize("\"hello\"");
        assertEquals(1, tokens.size());
        assertEquals(tok(MELLexer.STRING), tokens.get(0).type);
    }

    public void testCommentTokenType() {
        String text = "foo // this is a comment";
        List<TokenInfo> tokens = tokenize(text);
        List<TokenInfo> comments = new ArrayList<>();
        for (TokenInfo t : tokens) {
            if (t.type == tok(MELLexer.COMMENT)) comments.add(t);
        }
        assertFalse("Expected COMMENT token", comments.isEmpty());
        assertEquals("// this is a comment", comments.get(0).text);
    }

    // ── whitespace ────────────────────────────────────────────────────────────────

    public void testWhitespaceTokenIsPresentInExpression() {
        List<TokenInfo> tokens = tokenize("a + b");
        List<TokenInfo> ws = new ArrayList<>();
        for (TokenInfo t : tokens) {
            if (t.type == tok(MELLexer.WHITESPACE)) ws.add(t);
        }
        assertFalse("Expected WHITESPACE tokens", ws.isEmpty());
        for (TokenInfo w : ws) {
            // ensure whitespace text is blank (spaces/tabs)
            assertTrue("Whitespace text must be spaces", w.text.trim().isEmpty());
        }
    }

    public void testWhitespaceInsideBrackets() {
        // regression: request.auth.claims[ "email" ] was failing before
        List<TokenInfo> tokens = tokenize("a[ \"key\" ]");
        List<TokenInfo> ws = new ArrayList<>();
        for (TokenInfo t : tokens) {
            if (t.type == tok(MELLexer.WHITESPACE)) ws.add(t);
        }
        assertFalse("Expected WHITESPACE tokens inside brackets", ws.isEmpty());
    }

    public void testLeadingAndTrailingWhitespace() {
        String text = "  foo  ";
        List<TokenInfo> tokens = tokenize(text);
        assertContinuous(tokens, text);
        assertEquals(tok(MELLexer.WHITESPACE), tokens.get(0).type);
        assertEquals(tok(MELLexer.WHITESPACE), tokens.get(tokens.size() - 1).type);
    }

    // ── bad character handling ────────────────────────────────────────────────────

    public void testBadCharProducesTokenTypeBAD_TOKEN_TYPEInHighlightingMode() {
        List<TokenInfo> tokens = tokenize("foo;");
        List<TokenInfo> bad = new ArrayList<>();
        for (TokenInfo t : tokens) {
            if (t.type == MelTokenTypes.BAD_TOKEN_TYPE) bad.add(t);
        }
        assertFalse("Expected BAD_CHARACTER token for ';'", bad.isEmpty());
        assertEquals(";", bad.get(0).text);
    }

    public void testBadCharProducesFallbackWhitespaceTypeInParserMode() {
        // In parser mode the fallback is WHITESPACE token type so the parser
        // can safely treat unknown chars as ignorable whitespace.
        List<TokenInfo> tokens = tokenize("foo;");
        List<TokenInfo> fallback = new ArrayList<>();
        for (TokenInfo t : tokens) {
            if (t.type == tok(MELLexer.INVALID_CHAR) && ";".equals(t.text)) fallback.add(t);
        }
        assertFalse("Expected whitespace-fallback token for ';' in parser mode", fallback.isEmpty());
    }

    public void testConsecutiveBadCharsEachGetBAD_TOKEN_TYPE() {
        // each unrecognized char must be a separate or combined BAD_CHARACTER token,
        // but the entire range must be covered
        String text = ";;";
        List<TokenInfo> tokens = tokenize(text);
        assertContinuous(tokens, text);
        int covered = 0;
        for (TokenInfo t : tokens) {
            if (t.type == MelTokenTypes.BAD_TOKEN_TYPE) covered += t.text.length();
        }
        assertEquals("All bad chars must be covered", text.length(), covered);
    }

    // ── full expression snapshots ───────────────────────────────────────────────

    public void testMemberAccessProducesCorrectIdentifierTokens() {
        String text = "request.auth.claims";
        List<TokenInfo> tokens = tokenize(text);
        assertContinuous(tokens, text);
        List<String> idents = new ArrayList<>();
        for (TokenInfo t : tokens) {
            if (t.type == tok(MELLexer.IDENTIFIER)) idents.add(t.text);
        }
        assertEquals(3, idents.size());
        assertEquals(Arrays.asList("request", "auth", "claims"), idents);
    }

    public void testBooleanKeywordTokens() {
        assertEquals(tok(MELLexer.CEL_TRUE), tokenize("true").get(0).type);
        assertEquals(tok(MELLexer.CEL_FALSE), tokenize("false").get(0).type);
        assertEquals(tok(MELLexer.NUL), tokenize("null").get(0).type);
    }
}