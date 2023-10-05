package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.midpoint.studio.lang.axiomquery.psi.AQFilter;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryCompletionContributor extends CompletionContributor {
    public AxiomQueryCompletionContributor() {
        extend(null, psiElement().inside(psiElement(AQFilter.class)),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        resultSet.addAllElements(FilterNameProvider.ELEMENTS);
                    }
                });
    }
}
