package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.MidPointManager;
import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.impl.trace.Options;
import com.evolveum.midpoint.studio.impl.trace.TraceParser;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.PossiblyDumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.panels.Wrapper;
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

    public static final String NOTIFICATION_KEY = "Trace View";

    private Project project;

    private VirtualFile file;

    private Wrapper wrapper = new Wrapper();

    private TraceViewPanel traceViewPanel;

    public TraceViewEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;

        ApplicationManager.getApplication().invokeLater(() -> RunnableUtils.runReadAction(() -> initialize()));
    }

    private void initialize() {
        List<OpNode> data = new ArrayList<>();
        long start = 0;

        try (InputStream is = file.getInputStream()) {
            boolean isZip = file.getExtension().equalsIgnoreCase("zip");

            TraceParser parser = new TraceParser();
            data = parser.parse(is, isZip);
            start = parser.getStartTimestamp();
        } catch (Exception ex) {
            MidPointManager mm = MidPointManager.getInstance(project);
            mm.printToConsole(TraceViewEditor.class, "Couldn't load file", ex, ConsoleViewContentType.LOG_ERROR_OUTPUT);

            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Couldn't load file", ex);
        }

        traceViewPanel = new TraceViewPanel(project, data, start);
        wrapper.setContent(traceViewPanel);
    }

    public void applyOptions(Options options) {
        if (traceViewPanel != null) {
            traceViewPanel.applyOptions(options);
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return wrapper;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return wrapper;
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
        return file.isValid();
    }

    @Override
    public void selectNotify() {
        if (traceViewPanel != null) {
            traceViewPanel.selectNotify();
        }
    }

    @Override
    public void deselectNotify() {
        if (traceViewPanel != null) {
            traceViewPanel.deselectNotify();
        }
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
