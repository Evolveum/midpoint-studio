package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.cache.ObjectCache;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.XmlPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.evolveum.midpoint.studio.util.MidPointUtils.typesTag;
import static com.intellij.patterns.PlatformPatterns.psiElement;

public class SecretsProvidersCompletionContributor extends MidPointCompletionContributor {

    private record SecretsProviderItem(String id, String name, String type) {
    }

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

            EnvironmentCacheManager ecm = EnvironmentCacheManager.get(parameters.getPosition().getProject());
            ObjectCache<SystemConfigurationType> cache = ecm.getCache(EnvironmentCacheManager.KEY_SYSTEM_CONFIGURATION);
            SystemConfigurationType config = cache.get(SystemObjectsType.SYSTEM_CONFIGURATION.value());

            SecretsProvidersType providers = config.getSecretsProviders();
            if (providers == null) {
                return;
            }

            List<SecretsProviderItem> items = new ArrayList<>();
            items.addAll(createSecretProviderItems(List.of(providers.getDocker()), "Docker"));
            items.addAll(createSecretProviderItems(providers.getFile(), "File"));
            items.addAll(createSecretProviderItems(providers.getCustom(), "Custom"));
            items.addAll(createSecretProviderItems(providers.getProperties(), "Properties"));
            items.addAll(createSecretProviderItems(providers.getEnvironmentVariables(), "Environment Variables"));

            items.sort(Comparator.comparing(SecretsProviderItem::id));

            for (SecretsProviderItem item : items) {
                result.addElement(buildLookupElement(item.id(), item.name(), item.type()));
            }
        }

        private List<SecretsProviderItem> createSecretProviderItems(List<? extends SecretsProviderType> providers, String providerType) {
            return providers.stream()
                    .filter(provider -> provider != null)
                    .map(provider -> createSecretProviderItem(provider, providerType))
                    .toList();
        }

        private SecretsProviderItem createSecretProviderItem(SecretsProviderType provider, String providerType) {
            if (provider == null) {
                return null;
            }
            String name = null;
            if (provider.getDisplay() != null) {
                DisplayType display = provider.getDisplay();
                if (display.getLabel() != null) {
                    name = display.getLabel().getOrig();
                }
            }

            return new SecretsProviderItem(provider.getIdentifier(), name, providerType);
        }

        private LookupElement buildLookupElement(String value, String name, String providerType) {
            LookupElementBuilder builder = LookupElementBuilder.create(value)
                    .withTailText(name != null ? " (" + name + ")" : null)
                    .withTypeText(providerType)
                    .withLookupStrings(List.of(value, value.toLowerCase(), name, name.toLowerCase()))
                    .withBoldness(true)
                    .withCaseSensitivity(true);

            LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

            return PrioritizedLookupElement.withPriority(element, 200);
        }
    }
}
