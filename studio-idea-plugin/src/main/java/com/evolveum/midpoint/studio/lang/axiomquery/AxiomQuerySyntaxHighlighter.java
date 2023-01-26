package com.evolveum.midpoint.studio.lang.axiomquery;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryLexerV2.*;
import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQuerySyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey[] KEYWORD_KEYS =
            pack(createTextAttributesKey("AXIOM_QUERY_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD));

    public static final TextAttributesKey[] STRING_KEYS =
            pack(createTextAttributesKey("AXIOM_QUERY_STRING", DefaultLanguageHighlighterColors.STRING));

    public static final TextAttributesKey[] NUMBER_KEYS =
            pack(createTextAttributesKey("AXIOM_QUERY_NUMBER", DefaultLanguageHighlighterColors.NUMBER));

    public static final TextAttributesKey[] IDENTIFIER_KEYS =
            pack(createTextAttributesKey("AXIOM_QUERY_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER));

    public static final TextAttributesKey[] COMMENT_KEYS =
            pack(createTextAttributesKey("AXIOM_QUERY_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT));

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return AxiomQueryLexerAdaptor.newInstance();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (AxiomQueryTokenTypes.KEYWORDS.contains(tokenType)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(NULL)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(TRUE)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(FALSE)
        ) {
            return KEYWORD_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(STRING_SINGLEQUOTE)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(STRING_DOUBLEQUOTE)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(STRING_MULTILINE)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(STRING_BACKTICK)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(STRING_BACKTICK_TRIQOUTE)) {
            return STRING_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(INT)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(FLOAT)) {
            return NUMBER_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(IDENTIFIER)) {
            return IDENTIFIER_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(LINE_COMMENT)) {
            return COMMENT_KEYS;
        }

        return new TextAttributesKey[0];
    }
}
