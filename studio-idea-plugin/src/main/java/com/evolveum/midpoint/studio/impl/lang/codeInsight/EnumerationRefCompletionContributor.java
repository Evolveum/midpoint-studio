package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.cache.ObjectCache;
import com.evolveum.midpoint.studio.impl.lang.MidPointCompletionContributor;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LookupTableRowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LookupTableType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Support for <a:valueEnumerationRef> annotation (MID-9345)
 * <p>
 * Example: <a:valueEnumerationRef oid="00000000-0000-0000-0000-000000000230" type="tns:LookupTableType"/>
 *
 * Such annotation is read not through XSD types and PSI references, but via prism schema registry.
 */
public class EnumerationRefCompletionContributor extends MidPointCompletionContributor {

    public EnumerationRefCompletionContributor() {
        super();

        extend(CompletionType.BASIC,
                psiElement().inside(
                        XmlPatterns
                                .xmlText()),
                new EnumerationRefCompletionProvider());
    }

    private static class EnumerationRefCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(
                @NotNull CompletionParameters parameters,
                @NotNull ProcessingContext context,
                @NotNull CompletionResultSet result) {

            PsiElement position = parameters.getPosition().getParent();
            if (!(position instanceof XmlText text)) {
                return;
            }

            XmlTag tag = text.getParentTag();
            ItemDefinition<?> itemDefinition = PsiUtils.findItemDefinitionForTag(tag);
            if (itemDefinition == null) {
                return;
            }

            PrismReferenceValue ref = itemDefinition.getValueEnumerationRef();
            if (ref == null) {
                return;
            }

            Project project = position.getProject();
            ObjectCache<LookupTableType> cache = EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_LOOKUP_TABLE);
            LookupTableType table = cache.get(ref.getOid());
            if (table == null) {
                return;
            }

            table.getRow().forEach(row -> result.addElement(buildLookupElement(row.getKey(), getName(row))));
        }

        private String getName(LookupTableRowType row) {
            if (row.getLabel() == null) {
                return null;
            }

            return row.getLabel().getOrig();
        }

        private LookupElement buildLookupElement(String value, String name) {
            LookupElementBuilder builder = LookupElementBuilder.create(value)
                    .withTypeText(name)
                    .withLookupStrings(List.of(value, value.toLowerCase(), name, name.toLowerCase()))
                    .withBoldness(true)
                    .withCaseSensitivity(true);

            LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

            return PrioritizedLookupElement.withPriority(element, 200);
        }
    }
}
