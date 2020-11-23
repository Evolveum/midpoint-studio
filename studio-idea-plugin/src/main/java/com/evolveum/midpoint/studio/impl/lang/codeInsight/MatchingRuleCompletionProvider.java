package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.prism.PrismConstants;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MatchingRuleCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final List<LookupElement> RULES;

    static {
        List<QName> rules = new ArrayList<>();

        rules.add(PrismConstants.DEFAULT_MATCHING_RULE_NAME);
        rules.add(PrismConstants.STRING_IGNORE_CASE_MATCHING_RULE_NAME);
        rules.add(PrismConstants.POLY_STRING_STRICT_MATCHING_RULE_NAME);
        rules.add(PrismConstants.POLY_STRING_ORIG_MATCHING_RULE_NAME);
        rules.add(PrismConstants.POLY_STRING_NORM_MATCHING_RULE_NAME);
        rules.add(PrismConstants.DISTINGUISHED_NAME_MATCHING_RULE_NAME);
        rules.add(PrismConstants.EXCHANGE_EMAIL_ADDRESSES_MATCHING_RULE_NAME);
        rules.add(PrismConstants.UUID_MATCHING_RULE_NAME);
        rules.add(PrismConstants.XML_MATCHING_RULE_NAME);

        Collections.sort(rules, (r1, r2) -> String.CASE_INSENSITIVE_ORDER.compare(r1.getLocalPart(), r2.getLocalPart()));

        List<LookupElement> list = new ArrayList<>();
        for (QName s : rules) {
            list.add(buildLookupElement(s.getLocalPart(), "MatchingRule", 100));
        }

        RULES = Collections.unmodifiableList(list);
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(RULES);
    }

    private static LookupElement buildLookupElement(String name, String source, int priority) {
        LookupElementBuilder builder = LookupElementBuilder.create(name)
                .withTypeText(source)
                .withBoldness(true)
                .withCaseSensitivity(false);

        builder.withInsertHandler((context, item) -> {
            // todo insert annotation namespace correctly to parent element
        });

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, priority);
    }
}
