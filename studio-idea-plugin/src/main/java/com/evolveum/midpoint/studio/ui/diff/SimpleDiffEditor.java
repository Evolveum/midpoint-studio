package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.studio.ui.FileEditorBase;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SimpleDiffEditor extends FileEditorBase<DiffVirtualFile> {

    private static final String NAME = "Text";

    public SimpleDiffEditor(@NotNull Project project, @NotNull DiffVirtualFile file) {
        super(project, file);
    }

    @Override
    public @NotNull JComponent getComponent() {
        return getFile().getProcessor().getSimpleDiffComponent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return NAME;
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {
        // intentionally left empty
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void dispose() {
        // intentionally left empty
    }
}
