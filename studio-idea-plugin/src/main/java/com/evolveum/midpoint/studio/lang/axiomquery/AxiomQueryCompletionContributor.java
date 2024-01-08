package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryLangServiceImpl;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryCompletionContributor extends CompletionContributor {
    final AxiomQueryLangService axiomQueryLangService = new AxiomQueryLangServiceImpl(PrismContext.get());
    final List<LookupElement> suggestions = new ArrayList<>();

    public AxiomQueryCompletionContributor() {
        extend(null,
                PlatformPatterns.psiElement(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        suggestions.clear();

                        axiomQueryLangService.queryCompletion(
                                parameters.getOriginalFile().getText().substring(0, parameters.getPosition().getTextOffset())
                        ).forEach((filterName, alias) -> {
                            suggestions.add(build(filterName, alias));
                        });

                        if (!suggestions.isEmpty()) {
                            resultSet.addAllElements(suggestions);
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
