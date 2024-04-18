package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DiffSource(@Nullable String name, @NotNull VirtualFile file, @NotNull DiffSourceType type) {

    public String getName() {
        if (StringUtils.isNotEmpty(name)) {
            return name;
        }

        return file.getName();
    }
}
