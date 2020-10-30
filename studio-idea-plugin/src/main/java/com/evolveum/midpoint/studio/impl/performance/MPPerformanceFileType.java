package com.evolveum.midpoint.studio.impl.performance;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.openapi.fileTypes.UIBasedFileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * TODO choose better extension
 */
public class MPPerformanceFileType implements UIBasedFileType {

    public static final String DEFAULT_EXTENSION = "perf-sum";

    public static final MPPerformanceFileType INSTANCE = new MPPerformanceFileType();

    @NotNull
    @Override
    public String getName() {
        return "MPPerformance";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "MidPoint performance trace file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return MidPointIcons.ACTION_MIDPOINT;
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Nullable
    @Override
    public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
        return null;
    }
}
