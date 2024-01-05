package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryLangServiceImpl;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;
import com.evolveum.midpoint.studio.lang.axiomquery.psi.AQFilter;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;


/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryCompletionContributor extends CompletionContributor {
    AxiomQueryLangService axiomQueryLangService = new AxiomQueryLangServiceImpl(PrismContext.get());
    List<LookupElement> elements = new ArrayList<>();

    public AxiomQueryCompletionContributor() {
        extend(null, psiElement().inside(psiElement(AQFilter.class)),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {

                        elements.clear();

                        axiomQueryLangService.queryCompletion(parameters.getOriginalFile().getText()).forEach((filterName, alias) -> {
                            elements.add(build(filterName, alias));
                        });

                        if (!elements.isEmpty()) {
                            resultSet.addAllElements(elements);
                        }
                    }
                }
        );
    }

    private LookupElement build(String key, String alias) {
        if (alias == null) {
            alias = key;
        }

        LookupElementBuilder builder = LookupElementBuilder.create(key)
                .withTypeText(alias)
                .withLookupStrings(Arrays.asList(key, key.toLowerCase(), alias, alias.toLowerCase()))
                .withBoldness(true)
                .withCaseSensitivity(true);

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, 90);
    }

}
