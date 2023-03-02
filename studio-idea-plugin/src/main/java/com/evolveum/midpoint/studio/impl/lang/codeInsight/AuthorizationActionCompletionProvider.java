package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.model.api.ModelAuthorizationAction;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.util.QNameUtil;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AuthorizationActionCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final List<LookupElement> AUTHORIZATIONS;

    static {
        List<LookupElement> list = new ArrayList<>();
        for (QName q : getAuthorizationConstants()) {
            list.add(buildLookupElement(q, AuthorizationConstants.class));
        }
        for (QName q : getModelAuthorizations()) {
            list.add(buildLookupElement(q, ModelAuthorizationAction.class));
        }

        AUTHORIZATIONS = Collections.unmodifiableList(list);
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(AUTHORIZATIONS);
    }

    private static LookupElement buildLookupElement(QName q, Class clazz) {
        String name = QNameUtil.qNameToUri(q);

        LookupElement element = LookupElementBuilder.create(name)
                .withTailText("(" + q.getLocalPart() + ")")
                .withLookupString(name)
                .withLookupString(name.toLowerCase())
                .withLookupString(name.toUpperCase())
                .withTypeText(clazz.getSimpleName())
                .withCaseSensitivity(true)
                .withBoldness(true)
                .withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, 100);
    }

    private static List<QName> getModelAuthorizations() {
        List<QName> names = new ArrayList<>();

        for (ModelAuthorizationAction action : ModelAuthorizationAction.values()) {
            QName qname = QNameUtil.uriToQName(action.getUrl());
            names.add(qname);
        }

        Collections.sort(names, (q1, q2) -> String.CASE_INSENSITIVE_ORDER.compare(q1.getLocalPart(), q2.getLocalPart()));

        return names;
    }

    private static List<QName> getAuthorizationConstants() {
        List<Field> fields = FieldUtils.getAllFieldsList(AuthorizationConstants.class);
        Set<QName> qnames = new HashSet<>();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }

            Class type = field.getType();

            QName value = null;
            try {
                if (String.class.equals(type)) {
                    String uri = (String) field.get(AuthorizationConstants.class);
                    if (uri == null || !uri.startsWith(AuthorizationConstants.NS_SECURITY_PREFIX) || !uri.contains("#")) {
                        continue;
                    }

                    value = QNameUtil.uriToQName(uri);
                } else if (QName.class.equals(type)) {
                    value = (QName) field.get(AuthorizationConstants.class);
                }
            } catch (Exception e) {
                // nothing to do
            }

            if (value == null) {
                continue;
            }

            qnames.add(value);
        }

        List<QName> result = new ArrayList<>(qnames);
        Collections.sort(result, (q1, q2) -> String.CASE_INSENSITIVE_ORDER.compare(q1.getLocalPart(), q2.getLocalPart()));

        return result;
    }
}
