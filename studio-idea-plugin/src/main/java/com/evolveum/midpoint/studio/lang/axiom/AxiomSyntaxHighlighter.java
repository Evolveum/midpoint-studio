package com.evolveum.midpoint.studio.lang.axiom;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomSyntaxHighlighter extends SyntaxHighlighterBase {

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return null;
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return new TextAttributesKey[0];
    }
}
