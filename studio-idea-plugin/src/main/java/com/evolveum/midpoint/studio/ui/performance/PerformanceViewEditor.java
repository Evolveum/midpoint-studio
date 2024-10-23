package com.evolveum.midpoint.studio.ui.performance;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.impl.performance.PerformanceOptions;
import com.evolveum.midpoint.studio.impl.performance.PerformanceTree;
import com.evolveum.midpoint.studio.ui.performance.mainTree.PerformanceTreePanel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
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
import java.io.ObjectInputStream;

/**
 * TODO
 */
public class PerformanceViewEditor implements FileEditor, PossiblyDumbAware {

    private static final Logger LOG = Logger.getInstance(PerformanceViewEditor.class);

    public static final String NOTIFICATION_KEY = "Performance View";

    private final Project project;

    private final VirtualFile file;

    private final Wrapper wrapper = new Wrapper();

    private PerformanceTreePanel performanceTreePanel;

    public PerformanceViewEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;

        ApplicationManager.getApplication().invokeLater(() -> RunnableUtils.runReadAction(() -> initialize()));
    }

    private void initialize() {
        PerformanceTree tree;

        LOG.info("Initializing TraceViewEditor");
        try (InputStream is = file.getInputStream()) {
            ObjectInputStream ois = new ObjectInputStream(is);
            tree = (PerformanceTree) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            MidPointService mm = MidPointService.get(project);
            EnvironmentService es = EnvironmentService.getInstance(project);
            Environment env = es.getSelected();
            mm.printToConsole(env, getClass(), "Couldn't load file", ex, ConsoleViewContentType.LOG_ERROR_OUTPUT);
            MidPointUtils.publishExceptionNotification(mm.getProject(), env, getClass(), NOTIFICATION_KEY, "Couldn't load file", ex);
            ex.printStackTrace();
            tree = null;
        }

        performanceTreePanel = new PerformanceTreePanel(project, tree);
        wrapper.setContent(performanceTreePanel);
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
        return "Performance";
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
        if (performanceTreePanel != null) {
            performanceTreePanel.selectNotify();
        }
    }

    @Override
    public void deselectNotify() {
        if (performanceTreePanel != null) {
            performanceTreePanel.deselectNotify();
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

    public void applyOptions(PerformanceOptions options) {
        if (performanceTreePanel != null) {
            performanceTreePanel.applyOptions(options);
        }
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }
}
