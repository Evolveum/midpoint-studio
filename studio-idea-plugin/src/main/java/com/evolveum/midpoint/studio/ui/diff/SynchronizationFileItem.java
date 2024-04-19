package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SynchronizationFileItem(
        @NotNull VirtualFile local, List<SynchronizationObjectItem> objects) {
}
