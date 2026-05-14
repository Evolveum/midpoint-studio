package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.evolveum.midpoint.studio.lang.mel.antlr.MELLexer.*;
import static com.evolveum.midpoint.studio.lang.mel.impl.MelTokenTypes.getTokenElementType;

public class MelBraceMatcher implements PairedBraceMatcher {

    @NotNull
    @Override
    public BracePair @NotNull [] getPairs() {
        return new BracePair[]{
                new BracePair(getTokenElementType(LBRACKET), getTokenElementType(RPRACKET), true),
                new BracePair(getTokenElementType(LBRACE), getTokenElementType(RBRACE), true),
                new BracePair(getTokenElementType(LPAREN), getTokenElementType(RPAREN), true),
        };
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
