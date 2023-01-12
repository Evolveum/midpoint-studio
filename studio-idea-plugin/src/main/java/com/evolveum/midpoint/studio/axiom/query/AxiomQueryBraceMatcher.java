package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer.LEFT_BRACE;
import static com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer.RIGHT_BRACE;
import static com.evolveum.midpoint.studio.axiom.query.AxiomQueryTokenTypes.getTokenElementType;

public class AxiomQueryBraceMatcher implements PairedBraceMatcher {

    @NotNull
    @Override
    public BracePair[] getPairs() {
        return new BracePair[]{
                new BracePair(getTokenElementType(AxiomQueryLexer.T__7), getTokenElementType(AxiomQueryLexer.T__8), true),  // []
                new BracePair(getTokenElementType(AxiomQueryLexer.T__17), getTokenElementType(AxiomQueryLexer.T__19), true),  // ()
                new BracePair(getTokenElementType(LEFT_BRACE), getTokenElementType(RIGHT_BRACE), true), // {}
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
