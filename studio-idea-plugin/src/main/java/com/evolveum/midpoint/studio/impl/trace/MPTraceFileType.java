package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.UIBasedFileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MPTraceFileType implements UIBasedFileType {

    public static final String DEFAULT_EXTENSION = "mptrace";

    public static final MPTraceFileType INSTANCE = new MPTraceFileType();

    @NotNull
    @Override
    public String getName() {
        return "MPTrace";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "MidPoint Trace File or Archive";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return MidPointIcons.Midpoint;
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
