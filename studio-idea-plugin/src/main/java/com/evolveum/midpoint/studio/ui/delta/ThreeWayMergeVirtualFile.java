package com.evolveum.midpoint.studio.ui.delta;

import com.evolveum.midpoint.studio.impl.diff.InitialObjectDiffProcessor;
import com.intellij.openapi.vfs.VirtualFileWithoutContent;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

public class ThreeWayMergeVirtualFile extends LightVirtualFile implements VirtualFileWithoutContent {

    private final InitialObjectDiffProcessor processor;

    public ThreeWayMergeVirtualFile(@NotNull InitialObjectDiffProcessor processor) {
        this.processor = processor;
    }

    public InitialObjectDiffProcessor getProcessor() {
        return processor;
    }
}
