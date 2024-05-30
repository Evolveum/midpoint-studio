package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryLangServiceImpl;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;
import com.evolveum.midpoint.studio.lang.CompletionContributorBase;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryCompletionContributor extends CompletionContributorBase implements AxiomQueryHints {

    private final AxiomQueryLangService axiomQueryLangService = new AxiomQueryLangServiceImpl(PrismContext.get());

    public AxiomQueryCompletionContributor() {
        extend(null,
                PlatformPatterns.psiElement(),
                new CompletionProvider<>() {

                    @Override
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {

                        PsiElement element = parameters.getPosition();

                        ItemDefinition<?> def = null;

                        PsiElement outer = PsiUtils.getOuterPsiElement(element);
                        if (outer instanceof XmlText xmlText) {
                            // we have axiom query embedded in xml

                            XmlTag parentTag = xmlText.getParentTag();
                            def = PsiUtils.findItemDefinitionForTag(parentTag);
                        }

                        if (def == null) {
                            def = getObjectDefinitionFromHint(parameters.getEditor());
                        }

                        List<LookupElement> suggestions = new ArrayList<>();

                        String contentUpToCursor = parameters.getOriginalFile().getText()
                                .substring(0, parameters.getPosition().getTextOffset());

                        axiomQueryLangService.queryCompletion(def, contentUpToCursor)
                                .forEach((filterName, alias) -> suggestions.add(build(filterName, alias)));

                        resultSet.addAllElements(suggestions);
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
