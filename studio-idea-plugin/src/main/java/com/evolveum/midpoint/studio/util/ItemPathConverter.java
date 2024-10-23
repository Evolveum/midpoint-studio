package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemPathConverter extends Converter<ItemPath> {

    @Override
    public @Nullable ItemPath fromString(@NotNull String value) {
        return ItemPath.fromString(value);
    }

    @Override
    public @Nullable String toString(@NotNull ItemPath value) {
        return value.toString();
    }
}
