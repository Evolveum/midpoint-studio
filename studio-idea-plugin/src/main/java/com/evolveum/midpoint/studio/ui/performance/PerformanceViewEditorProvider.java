package com.evolveum.midpoint.studio.ui.performance;

import com.evolveum.midpoint.studio.impl.performance.MPPerformanceFileType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class PerformanceViewEditorProvider implements FileEditorProvider, DumbAware {

    private static final String EDITOR_TYPE_ID = "performance-view-ui";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        String ext = file.getExtension();
        return MPPerformanceFileType.DEFAULT_EXTENSION.equalsIgnoreCase(ext);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new PerformanceViewEditor(project, file);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
