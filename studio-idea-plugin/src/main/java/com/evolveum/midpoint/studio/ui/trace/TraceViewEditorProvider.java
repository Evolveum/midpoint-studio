package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.MPTraceFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceViewEditorProvider implements FileEditorProvider, DumbAware {

    private static final Logger LOG = Logger.getInstance(TraceViewEditorProvider.class);

    private static final String EDITOR_TYPE_ID = "trace-view-ui";

    private static final String FILE_DATA_PREFIX = "<tracingOutput";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        String ext = file.getExtension();

        if (MPTraceFileType.DEFAULT_EXTENSION.equalsIgnoreCase(ext)) {
            return true;
        }

        if ("zip".equalsIgnoreCase(ext)) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry zipEntry = zis.getNextEntry();
                if (zipEntry != null) {
                    byte[] data = new byte[FILE_DATA_PREFIX.length()];
                    zis.read(data, 0, data.length);
                    if (FILE_DATA_PREFIX.equals(new String(data))) {
                        return true;
                    }
                }
            } catch (IOException ex) {
                LOG.debug("Couldn't check " + file.getPath(), ex.getMessage());
            }
        }

        return false;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new TraceViewEditor(project, file);
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
