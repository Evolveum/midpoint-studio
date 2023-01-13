package com.evolveum.midpoint.studio.axiom.query.lang;

import com.evolveum.midpoint.studio.axiom.query.psi.AQFilter;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.DefaultCompletionContributor;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryCompletionContributor extends DefaultCompletionContributor {

    public AxiomQueryCompletionContributor() {
        extend(CompletionType.BASIC,
                psiElement().inside(
                        psiElement(AQFilter.class)),
                new FilterNameProvider());
    }
}
