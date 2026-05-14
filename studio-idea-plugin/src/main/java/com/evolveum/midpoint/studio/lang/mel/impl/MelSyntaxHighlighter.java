package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer.*;
import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MelSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey[] KEYWORD_KEYS =
            pack(createTextAttributesKey("CEL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD));

    public static final TextAttributesKey[] STRING_KEYS =
            pack(createTextAttributesKey("CEL_STRING", DefaultLanguageHighlighterColors.STRING));

    public static final TextAttributesKey[] NUMBER_KEYS =
            pack(createTextAttributesKey("CEL_NUMBER", DefaultLanguageHighlighterColors.NUMBER));

    public static final TextAttributesKey[] IDENTIFIER_KEYS =
            pack(createTextAttributesKey("CEL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER));

    public static final TextAttributesKey[] BRACES_KEYS =
            pack(createTextAttributesKey("CEL_BRACES", DefaultLanguageHighlighterColors.BRACES));

    public static final TextAttributesKey[] PARENTHESES_KEYS =
            pack(createTextAttributesKey("CEL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES));

    public static final TextAttributesKey[] BRACKETS_KEYS =
            pack(createTextAttributesKey("CEL_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS));

    public static final TextAttributesKey[] COMMENT_KEYS =
            pack(createTextAttributesKey("CEL_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT),
                    createTextAttributesKey("CEL_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT));

    private static final TextAttributesKey[] BAD_CHAR_KEYS = pack(HighlighterColors.BAD_CHARACTER);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return MelLexerAdaptor.newInstance();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (MelTokenTypes.KEYWORDS.contains(tokenType)
                || tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(CEL_TRUE)
                || tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(CEL_FALSE)
                || tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(NUL)
        ) {
            return KEYWORD_KEYS;
        }

        if (tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(STRING)) {
            return STRING_KEYS;
        }

        if (tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(NUM_INT)
                || tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(NUM_UINT)
                || tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(NUM_FLOAT)) {
            return NUMBER_KEYS;
        }

        if (tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(IDENTIFIER)) {
            return IDENTIFIER_KEYS;
        }

        if (tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(LBRACE)
                || tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(RBRACE)) {
            return BRACES_KEYS;
        }

        if (tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(LPAREN)
                || tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(RPAREN)) {
            return PARENTHESES_KEYS;
        }

        if (tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(LBRACKET)
                || tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(RPRACKET)) {
            return BRACKETS_KEYS;
        }

        if (tokenType == MelTokenTypes.TOKEN_ELEMENT_TYPES.get(COMMENT)) {
            return COMMENT_KEYS;
        }

        if (tokenType == MelTokenTypes.BAD_TOKEN_TYPE) {
            return BAD_CHAR_KEYS;
        }

        return new TextAttributesKey[0];
    }
}
