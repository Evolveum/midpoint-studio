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

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("AXIOM_QUERY_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey STRING =
            createTextAttributesKey("AXIOM_QUERY_STRING", DefaultLanguageHighlighterColors.STRING);

    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("AXIOM_QUERY_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);

    public static final TextAttributesKey[] STRING_KEYS = pack(STRING);

    public static final TextAttributesKey[] IDENTIFIER_KEYS = pack(IDENTIFIER);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        AxiomQueryLexer lexer = new AxiomQueryLexer(null);
        return new AxiomQueryLexerAdaptor(lexer);
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.STRING_DOUBLEQUOTE)) {
            return STRING_KEYS;
        }

        if (tokenType == AxiomQueryTokenTypes.TOKEN_ELEMENT_TYPES.get(AxiomQueryLexer.IDENTIFIER)) {
            return IDENTIFIER_KEYS;
        }

        return new TextAttributesKey[0];
    }
}
