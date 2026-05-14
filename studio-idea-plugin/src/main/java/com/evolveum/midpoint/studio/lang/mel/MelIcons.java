package com.evolveum.midpoint.studio.lang.mel;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MelIcons {

    public static final @NotNull Icon ICON_FILE_TYPE = load("icons/file-type.svg");

    private static @NotNull Icon load(@NotNull String path) {
        return IconLoader.getIcon(path, MelIcons.class);
    }
}
