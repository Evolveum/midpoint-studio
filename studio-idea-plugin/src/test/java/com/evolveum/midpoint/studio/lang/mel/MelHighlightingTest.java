package com.evolveum.midpoint.studio.lang.mel;

import com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer;
import com.evolveum.midpoint.studio.lang.mel.impl.MelFileType;
import com.evolveum.midpoint.studio.lang.mel.impl.MelSyntaxHighlighter;
import com.evolveum.midpoint.studio.lang.mel.impl.MelTokenTypes;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;

public class MelHighlightingTest extends BasePlatformTestCase {

    // ── helpers ───────────────────────────────────────────────────────────────────

    private final MelSyntaxHighlighter hl = new MelSyntaxHighlighter();

    private TextAttributesKey[] highlightsForToken(int index) {
        return hl.getTokenHighlights(MelTokenTypes.TOKEN_ELEMENT_TYPES.toArray(new TokenIElementType[0])[index]);
    }

    private void assertHighlighted(int index, String description) {
        assertTrue(description + " should be highlighted", highlightsForToken(index).length != 0);
    }

    private void assertNotHighlighted(int index, String description) {
        assertTrue(description + " should NOT be highlighted", highlightsForToken(index).length == 0);
    }

    // ── token → color mapping ─────────────────────────────────────────────────────

    public void testKeywordsAreHighlighted() {
        assertHighlighted(MELLexer.LOGICAL_AND, "&&");
        assertHighlighted(MELLexer.LOGICAL_OR, "||");
        assertHighlighted(MELLexer.CEL_TRUE, "true");
        assertHighlighted(MELLexer.CEL_FALSE, "false");
        assertHighlighted(MELLexer.NUL, "null");
    }

    public void testStringIsHighlighted() {
        assertHighlighted(MELLexer.STRING, "STRING");
    }

    public void testNumbersAreHighlighted() {
        assertHighlighted(MELLexer.NUM_INT, "NUM_INT");
        assertHighlighted(MELLexer.NUM_UINT, "NUM_UINT");
        assertHighlighted(MELLexer.NUM_FLOAT, "NUM_FLOAT");
    }

    public void testIdentifierIsHighlighted() {
        assertHighlighted(MELLexer.IDENTIFIER, "IDENTIFIER");
    }

    public void testBracesAreHighlighted() {
        assertHighlighted(MELLexer.LBRACE, "{");
        assertHighlighted(MELLexer.RBRACE, "}");
    }

    public void testBracketsAreHighlighted() {
        assertHighlighted(MELLexer.LBRACKET, "[");
        assertHighlighted(MELLexer.RPRACKET, "]");
    }

    public void testParenthesesAreHighlighted() {
        assertHighlighted(MELLexer.LPAREN, "(");
        assertHighlighted(MELLexer.RPAREN, ")");
    }

    public void testCommentIsHighlighted() {
        assertHighlighted(MELLexer.COMMENT, "COMMENT");
    }

    public void testWhitespaceIsNotHighlighted() {
        assertNotHighlighted(MELLexer.WHITESPACE, "WHITESPACE");
    }

    public void testBadCharacterIsHighlightedAsError() {
        TextAttributesKey[] keys = hl.getTokenHighlights(MelTokenTypes.BAD_TOKEN_TYPE);
        assertTrue(
                "BAD_TOKEN_TYPE token must produce error highlighting (fix MelSyntaxHighlighter)",
                keys.length != 0
        );
    }

    // ── file-based: valid file must produce no errors ─────────────────────────────

    public void testNoHighlightingErrorsInValidExpression() {
        // testHighlighting() fails if there are unexpected error-level highlights
        myFixture.configureByText(MelFileType.INSTANCE, "request.auth.claims[\"email\"]");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testNoHighlightingErrorsInExpressionWithSpaces() {
        myFixture.configureByText(MelFileType.INSTANCE, "request.auth.claims[ \"email\" ]");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testNoHighlightingErrorsWithComment() {
        myFixture.configureByText(MelFileType.INSTANCE, "foo // comment");
        myFixture.checkHighlighting(false, false, false);
    }
}