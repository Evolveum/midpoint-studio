package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer;
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
public class StudioPropertiesSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey[] KEYWORD_KEYS =
            pack(createTextAttributesKey("STUDIO_PROPERTIES_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD));

    public static final TextAttributesKey[] STRING_KEYS =
            pack(createTextAttributesKey("STUDIO_PROPERTIES_STRING", DefaultLanguageHighlighterColors.STRING));

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return StudioPropertiesLexerAdaptor.newInstance();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (StudioPropertiesTokenTypes.KEYWORDS.contains(tokenType)) {
            return KEYWORD_KEYS;
        }

        if (StudioPropertiesTokenTypes.STRINGS.contains(tokenType)
                || tokenType == StudioPropertiesTokenTypes.TOKEN_ELEMENT_TYPES.get(StudioPropertiesLexer.SLASH)
                || tokenType == StudioPropertiesTokenTypes.TOKEN_ELEMENT_TYPES.get(StudioPropertiesLexer.PATH_SELF)
                || tokenType == StudioPropertiesTokenTypes.TOKEN_ELEMENT_TYPES.get(StudioPropertiesLexer.PATH_PARENT)) {
            return STRING_KEYS;
        }

        return new TextAttributesKey[0];
    }
}
