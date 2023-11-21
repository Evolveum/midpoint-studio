package com.evolveum.midpoint.sdk.impl.lang;

import com.evolveum.midpoint.model.api.ModelAuthorizationAction;
import com.evolveum.midpoint.sdk.api.lang.Action;
import com.evolveum.midpoint.sdk.api.lang.AuthorizationActionProvider;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.util.QNameUtil;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuthorizationActionProviderImpl implements AuthorizationActionProvider {

    private static final Set<Action> ACTIONS;

    static {
        Set<Action> actions = new HashSet<>();

        actions.addAll(getAuthorizationConstants());
        actions.addAll(getModelAuthorizations());

        ACTIONS = Collections.unmodifiableSet(actions);
    }

    @Override
    public Set<Action> getActions() {
        return ACTIONS;
    }

    private static Set<Action> getModelAuthorizations() {
        Set<Action> actions = new HashSet<>();

        for (ModelAuthorizationAction action : ModelAuthorizationAction.values()) {
            QName qname = QNameUtil.uriToQName(action.getUrl());
            actions.add(new Action(qname, ModelAuthorizationAction.class.getName()));
        }

        return actions;
    }

    private static Set<Action> getAuthorizationConstants() {
        List<Field> fields = FieldUtils.getAllFieldsList(AuthorizationConstants.class);
        Set<Action> actions = new HashSet<>();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }

            Class<?> type = field.getType();

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

            actions.add(new Action(value, AuthorizationConstants.class.getName()));
        }

        return actions;
    }
}
