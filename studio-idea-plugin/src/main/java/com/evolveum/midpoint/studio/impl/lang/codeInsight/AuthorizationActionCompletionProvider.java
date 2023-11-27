package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.sdk.api.lang.Action;
import com.evolveum.midpoint.sdk.api.lang.AuthorizationActionProvider;
import com.evolveum.midpoint.util.QNameUtil;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AuthorizationActionCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final List<LookupElement> AUTHORIZATIONS;

    static {
        AuthorizationActionProvider provider = null;    // todo inject provider
        Set<Action> actions = provider != null ? provider.getActions() : Collections.emptySet();

        List<Action> list = new ArrayList<>(actions);
        list.sort(Action::compareTo);

        AUTHORIZATIONS = list.stream()
                .map(action -> buildLookupElement(action.name(), action.source()))
                .toList();
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(AUTHORIZATIONS);
    }

    private static LookupElement buildLookupElement(QName q, String sourceClass) {
        String name = QNameUtil.qNameToUri(q);

        String clazz = sourceClass != null ? ClassUtils.getShortClassName(sourceClass) : "";

        LookupElement element = LookupElementBuilder
                .create(name)
                .withTailText("(" + q.getLocalPart() + ")")
                .withLookupString(name)
                .withLookupString(name.toLowerCase())
                .withLookupString(name.toUpperCase())
                .withTypeText(clazz)
                .withCaseSensitivity(true)
                .withBoldness(true)
                .withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, 100);
    }
}
