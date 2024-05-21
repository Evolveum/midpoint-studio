package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFileWithoutContent;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DiffVirtualFile extends LightVirtualFile implements VirtualFileWithoutContent {

    private final DiffProcessor processor;

    public DiffVirtualFile(@NotNull DiffProcessor processor) {
        super(processor.getName());

        this.processor = processor;
    }

    public DiffProcessor getProcessor() {
        return processor;
    }

    @Override
    public @NlsSafe @NotNull String getName() {
        return super.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffVirtualFile that = (DiffVirtualFile) o;
        return Objects.equals(processor, that.processor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(processor);
    }
}
