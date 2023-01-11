package com.evolveum.midpoint.studio.ui.profiler;

import com.evolveum.midpoint.studio.util.MidPointUtils;
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
public class ProfilingEditorProvider implements FileEditorProvider, DumbAware {

    private static final String EDITOR_TYPE_ID="profiling-ui";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return false;
        }

        return file.getExtension().equalsIgnoreCase("aaa");
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new ProfilingEditor(project);
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
