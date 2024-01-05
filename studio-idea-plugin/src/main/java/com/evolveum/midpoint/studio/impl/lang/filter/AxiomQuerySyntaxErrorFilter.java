package com.evolveum.midpoint.studio.impl.lang.filter;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.psi.PsiErrorElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Dominik.
 */
public class AxiomQuerySyntaxErrorFilter extends HighlightErrorFilter {
    @Override
    public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement element) {
        return false;
    }
}
