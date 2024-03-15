package com.evolveum.midpoint.studio.impl.lang;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.XmlPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.evolveum.midpoint.studio.util.MidPointUtils.typesTag;
import static com.intellij.patterns.PlatformPatterns.psiElement;

public class SecretsProvidersCompletionContributor extends MidPointCompletionContributor {

    public SecretsProvidersCompletionContributor() {
        super();

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns
                                .xmlText()
                                .withParent(
                                        typesTag("provider").withParent(typesTag("externalData"))
                                )),
                new SecretsProvidersCompletionProvider());
    }

    private static class SecretsProvidersCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(
                @NotNull CompletionParameters parameters,
                @NotNull ProcessingContext context,
                @NotNull CompletionResultSet result) {

            // todo implement
            //result.addElement(buildLookupElement("sample-provider", "Docker"));
        }

        private LookupElement buildLookupElement(String value, String providerType) {
            LookupElementBuilder builder = LookupElementBuilder.create(value)
                    .withTypeText(providerType)
                    .withLookupStrings(Arrays.asList(value, value.toLowerCase()))
                    .withBoldness(true)
                    .withCaseSensitivity(true);

            LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

            return PrioritizedLookupElement.withPriority(element, 200);
        }
    }
}
