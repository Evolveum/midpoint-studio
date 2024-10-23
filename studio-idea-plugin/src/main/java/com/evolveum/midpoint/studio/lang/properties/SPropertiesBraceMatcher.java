package com.evolveum.midpoint.studio.lang.properties;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.evolveum.midpoint.studio.lang.properties.SPropertiesTokenTypes.getTokenElementType;
import static com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer.LEFT_BRACKET;
import static com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesLexer.RIGHT_BRACKET;

public class SPropertiesBraceMatcher implements PairedBraceMatcher {

    @NotNull
    @Override
    public BracePair[] getPairs() {
        return new BracePair[]{
                new BracePair(getTokenElementType(LEFT_BRACKET), getTokenElementType(RIGHT_BRACKET), true),
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
