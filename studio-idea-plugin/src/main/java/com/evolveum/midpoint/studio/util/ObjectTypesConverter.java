package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.intellij.util.xmlb.Converter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectTypesConverter extends Converter<List<ObjectTypes>> {

    public static @Nullable List<ObjectTypes> fromString(@NotNull String value, boolean ignoreWrongValue) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }

        List<ObjectTypes> result = new ArrayList<>();

        String[] types = value.split(",");
        for (String type : types) {
            try {
                ObjectTypes ot = ObjectTypes.getObjectType(type);
                if (ot != null) {
                    result.add(ot);
                }
            } catch (RuntimeException ex) {
                if (!ignoreWrongValue) {
                    throw ex;
                }
            }
        }

        return result;
    }

    @Override
    public @Nullable List<ObjectTypes> fromString(@NotNull String value) {
        return fromString(value, true);
    }

    @Override
    public @Nullable String toString(@NotNull List<ObjectTypes> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        List<String> types = new ArrayList<>();
        value.forEach(v -> types.add(v.getValue()));

        return StringUtils.join(types, ",");
    }
}
