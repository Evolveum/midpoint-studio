package com.evolveum.midpoint.studio.axiom.query.lang;

import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ProcessingContext;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class FilterNameProvider extends CompletionProvider<CompletionParameters> {

    private static final Logger LOG = Logger.getInstance(FilterNameProvider.class);

    private static final List<LookupElement> ELEMENTS;

    static {
        List<LookupElement> result;

        try {
            final Class filterNames = Class.forName("com.evolveum.midpoint.prism.impl.query.lang.FilterNames");

            final Method aliasFor = filterNames.getDeclaredMethod("aliasFor", QName.class);

            final List<QName> filters = ReflectionUtil.collectFields(filterNames).stream()
                    .filter(f -> QName.class.isAssignableFrom(f.getType()))
                    .map(f -> {
                        try {
                            f.setAccessible(true);
                            return (QName) f.get(null);
                        } catch (Exception ex) {
                            LOG.warn("Couldn't access field to get filter name (QName) " + f, ex);
                            return null;
                        } finally {
                            f.setAccessible(false);
                        }
                    })
                    .filter(q -> q != null)
                    .collect(Collectors.toList());

            final List<Pair<String, String>> list = new ArrayList<>();

            filters.forEach(q -> {

                String alias = null;
                try {
                    aliasFor.setAccessible(true);
                    alias = ((Optional<String>) aliasFor.invoke(null, q)).orElse(null);
                } catch (Exception ex) {
                    LOG.warn("Couldn't obtain filter name alias for filter " + q, ex);
                } finally {
                    aliasFor.setAccessible(false);
                }

                if (alias != null) {
                    list.add(new Pair<>(alias, q.getLocalPart()));
                    list.add(new Pair<>(q.getLocalPart(), alias));
                } else {
                    list.add(new Pair<>(q.getLocalPart(), null));
                }
            });

            list.sort(Comparator.comparing(Pair::getFirst));

            result = list.stream()
                    .map(p -> build(p.getFirst(), p.getSecond()))
                    .collect(Collectors.toUnmodifiableList());
        } catch (Exception ex) {
            LOG.warn("Couldn't prepare list of filter names for completion", ex);

            result = Collections.emptyList();
        }

        ELEMENTS = result;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(ELEMENTS);
    }

    private static LookupElement build(String key, String alias) {
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
