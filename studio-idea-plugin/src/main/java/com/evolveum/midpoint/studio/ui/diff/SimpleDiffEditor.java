package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismObject;
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

        PrismObject left = processor.getLeftObject();
        PrismObject right = processor.getRightObject();

        panel = new SimpleDiffPanel<>(project, left, processor.getLeftDiffSourceType(), right, processor.getRightDiffSourceType());
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
    }

    @Override
    public boolean isModified() {
        // todo implement
        return true;
    }

    @Override
    public boolean isValid() {
        // todo implement
        return true;
    }

    @Override
    public void dispose() {

    }
}
