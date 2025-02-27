package com.evolveum.midpoint.studio.lang.axiomquery;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer.*;
import static com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryTokenTypes.getTokenElementType;

public class AxiomQueryBraceMatcher implements PairedBraceMatcher {

    @NotNull
    @Override
    public BracePair @NotNull [] getPairs() {
        return new BracePair[]{
                new BracePair(getTokenElementType(SQUARE_BRACKET_LEFT), getTokenElementType(SQUARE_BRACKET_RIGHT), true),
                new BracePair(getTokenElementType(ROUND_BRACKET_LEFT), getTokenElementType(ROUND_BRACKET_RIGHT), true),
                new BracePair(getTokenElementType(LEFT_BRACE), getTokenElementType(RIGHT_BRACE), true),
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
