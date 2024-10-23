package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SimpleDiffEditorProvider implements FileEditorProvider, DumbAware {

    private static final String EDITOR_TYPE_ID = "simple-diff-editor";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return file instanceof DiffVirtualFile;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new SimpleDiffEditor(project, (DiffVirtualFile) file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}

