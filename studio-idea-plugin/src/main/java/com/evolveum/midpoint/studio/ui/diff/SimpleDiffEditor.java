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

    private SimpleDiffPanel panel;

    public SimpleDiffEditor(@NotNull Project project, @NotNull DiffVirtualFile file) {
        super(project, file);

        DiffProcessor processor = file.getProcessor();

        panel = new SimpleDiffPanel<>(project, processor.getLeftSource(), processor.getRightSource());
    }

    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return panel.getPreferredFocusedComponent();
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
        // todo implement
        return true;
    }

    @Override
    public void dispose() {
        // todo implement
    }
}
