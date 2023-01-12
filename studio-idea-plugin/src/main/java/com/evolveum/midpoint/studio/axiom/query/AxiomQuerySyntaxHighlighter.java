package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

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
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.T__0)   // null
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.T__1)   // true
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.T__2)   // false
        ) {
            return KEYWORD_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.STRING_SINGLEQUOTE)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.STRING_DOUBLEQUOTE)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.STRING_MULTILINE)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.STRING_BACKTICK)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.STRING_BACKTICK_TRIQOUTE)) {
            return STRING_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.INT)
                || tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.FLOAT)) {
            return NUMBER_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.IDENTIFIER)) {
            return IDENTIFIER_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.LINE_COMMENT)) {
            return COMMENT_KEYS;
        }

        return new TextAttributesKey[0];
    }
}
