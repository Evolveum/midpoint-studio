package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record FileItem(
        @NotNull VirtualFile local, List<ObjectItem> objects) {
}
