package com.evolveum.midpoint.sdk.impl.lang;

import com.evolveum.midpoint.model.api.ModelPublicConstants;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.sdk.api.lang.TaskHandlerProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TaskHandlerProviderImpl implements TaskHandlerProvider {

    public static final Set<String> HANDLERS;

    static {
        Set<String> handlers = new HashSet<>();

        Field[] fields = ModelPublicConstants.class.getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers())) {
                // not public static final
                continue;
            }

            if (String.class != field.getType()) {
                continue;
            }

            try {
                String value = (String) field.get(ModelPublicConstants.class);
                if (value == null || !value.startsWith(SchemaConstants.NS_MODEL)) {
                    continue;
                }

                handlers.add(value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
            }
        }

        HANDLERS = Collections.unmodifiableSet(handlers);
    }

    @Override
    public Set<String> getTaskHandlerUris() {
        return HANDLERS;
    }
}
