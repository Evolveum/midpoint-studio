package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.model.api.ModelAuthorizationAction;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.QNameUtil;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        return MidPointUtils.buildLookupElement(q.getLocalPart(), q.getNamespaceURI() + "#" + q.getLocalPart(), clazz.getSimpleName(), 100);
    }

    private static List<QName> getModelAuthorizations() {
        List<QName> qnames = new ArrayList<>();

        for (ModelAuthorizationAction action : ModelAuthorizationAction.values()) {
            QName qname = QNameUtil.uriToQName(action.getUrl());
            qnames.add(qname);
        }

        Collections.sort(qnames, (q1, q2) -> String.CASE_INSENSITIVE_ORDER.compare(q1.getLocalPart(), q2.getLocalPart()));

        return qnames;
    }

    private static List<QName> getAuthorizationConstants() {
        List<Field> fields = FieldUtils.getAllFieldsList(AuthorizationConstants.class);
        List<QName> qnames = new ArrayList<>();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }

            Class type = field.getType();
            if (!QName.class.equals(type)) {
                continue;
            }

            QName value = null;
            try {
                value = (QName) field.get(AuthorizationConstants.class);
            } catch (IllegalAccessException e) {
                // nothing to do
            }

            if (value == null) {
                continue;
            }

            qnames.add(value);
        }

        Collections.sort(qnames, (q1, q2) -> String.CASE_INSENSITIVE_ORDER.compare(q1.getLocalPart(), q2.getLocalPart()));

        return qnames;
    }
}
