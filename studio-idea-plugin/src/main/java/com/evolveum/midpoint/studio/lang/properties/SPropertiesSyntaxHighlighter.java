package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey[] KEYWORD_KEYS =
            pack(createTextAttributesKey("STUDIO_PROPERTIES_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD));

    public static final TextAttributesKey[] STRING_KEYS =
            pack(createTextAttributesKey("STUDIO_PROPERTIES_STRING", DefaultLanguageHighlighterColors.STRING));

    private static final TextAttributesKey[] BAD_CHAR_KEYS = pack(HighlighterColors.BAD_CHARACTER);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return SPropertiesLexerAdaptor.newInstance();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (SPropertiesTokenTypes.KEYWORDS.contains(tokenType)) {
            return KEYWORD_KEYS;
        }

        if (SPropertiesTokenTypes.STRINGS.contains(tokenType)
                || tokenType == SPropertiesTokenTypes.TOKEN_ELEMENT_TYPES.get(StudioPropertiesLexer.SLASH)
                || tokenType == SPropertiesTokenTypes.TOKEN_ELEMENT_TYPES.get(StudioPropertiesLexer.PATH_SELF)
                || tokenType == SPropertiesTokenTypes.TOKEN_ELEMENT_TYPES.get(StudioPropertiesLexer.PATH_PARENT)) {
            return STRING_KEYS;
        }

        if (tokenType == SPropertiesTokenTypes.BAD_TOKEN_TYPE) {
            return BAD_CHAR_KEYS;
        }

        return new TextAttributesKey[0];
    }
}
