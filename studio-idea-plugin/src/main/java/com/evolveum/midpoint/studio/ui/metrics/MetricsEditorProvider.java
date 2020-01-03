package com.evolveum.midpoint.studio.ui.metrics;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsEditorProvider implements FileEditorProvider, DumbAware {

    public static final String METRICS_FILE_EXTENSION = "mpmetrics";

    private static final String EDITOR_TYPE_ID = "profiling-ui";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return file.getExtension().equalsIgnoreCase(METRICS_FILE_EXTENSION);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new MetricsEditor(project, file);
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
