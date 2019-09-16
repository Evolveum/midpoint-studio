package com.evolveum.midpoint.studio.action.browse;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class AbstractDownloadAction extends AnAction implements DumbAware {

    private DownloadOptions options;

    public AbstractDownloadAction(@Nullable String text, @Nullable Icon icon, @NotNull DownloadOptions options) {
        super(text, text, icon);
        this.options = options;
    }
}
