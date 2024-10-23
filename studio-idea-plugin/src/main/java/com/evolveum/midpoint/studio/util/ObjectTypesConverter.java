package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ObjectTypesConverter extends Converter<ObjectTypes> {

    @Override
    public @Nullable ObjectTypes fromString(@NotNull String value) {
        for (ObjectTypes type : ObjectTypes.values()) {
            if (Objects.equals(value, type.getValue())) {
                return type;
            }
        }

        return null;
    }

    @Override
    public @Nullable String toString(@NotNull ObjectTypes value) {
        return value.getValue();
    }
}
