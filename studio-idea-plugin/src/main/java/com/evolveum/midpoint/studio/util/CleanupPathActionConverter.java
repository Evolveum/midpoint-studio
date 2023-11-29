package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.impl.configuration.CleanupPathAction;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CleanupPathActionConverter extends Converter<CleanupPathAction> {

    @Override
    public @Nullable CleanupPathAction fromString(@NotNull String value) {
        return CleanupPathAction.getState(value);
    }

    @Override
    public @Nullable String toString(@NotNull CleanupPathAction value) {
        return value.value();
    }
}
