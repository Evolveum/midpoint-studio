package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.impl.trace.TraceParser;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.PossiblyDumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceViewEditor implements FileEditor, PossiblyDumbAware {

    private Project project;

    private VirtualFile file;

    private TraceViewPanel panel;

    public TraceViewEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;

        initEditor();
    }

    private void initEditor() {
        List<OpNode> data = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            //new File("/Users/lazyman/Work/monoted/projects/ek/midpoint-4.0-SNAPSHOT/var/trace/functional-trace 2019-10-29-16-40-06-677 iadubdor.zip");

            boolean isZip = file.getExtension().equalsIgnoreCase("zip");

            data = new TraceParser().parse(is, isZip);
        } catch (Exception e) {
            e.printStackTrace();    // todo error handling
        }

        panel = new TraceViewPanel(data);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return panel;
    }

    @NotNull
    @Override
    public String getName() {
        return "Trace";
    }   // todo improve

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;    // todo improve
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
