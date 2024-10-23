package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.OpNodeTreeBuilder;
import com.evolveum.midpoint.schema.traces.TraceParser;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.impl.trace.Options;
import com.evolveum.midpoint.studio.impl.trace.StudioNameResolver;
import com.evolveum.midpoint.studio.ui.trace.mainTree.OpTreePanel;
import com.evolveum.midpoint.studio.ui.trace.presentation.PresentationInitializer;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TracingOutputType;
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
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.panels.Wrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceViewEditor implements FileEditor, UserDataHolder, PossiblyDumbAware {

    private static final Logger LOG = Logger.getInstance(TraceViewEditor.class);

    public static final String NOTIFICATION_KEY = "Trace View";

    private UserDataHolderBase userDataHolder = new UserDataHolderBase();

    private Project project;

    private VirtualFile file;

    private Wrapper wrapper = new Wrapper();

    private OpTreePanel opTreePanel;

    public TraceViewEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;

        ApplicationManager.getApplication().invokeLater(() -> RunnableUtils.runReadAction(() -> initialize()));
    }

    private void initialize() {
        OpNode root;

        long start = System.currentTimeMillis();
        LOG.info("Initializing TraceViewEditor");
        try (InputStream is = file.getInputStream()) {
            String extension = file.getExtension();
            boolean isZip = extension != null && extension.equalsIgnoreCase("zip");

            PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT;
            TraceParser parser = new TraceParser(prismContext);
            TracingOutputType tracingOutput = parser.parse(is, isZip, file.getCanonicalPath());
            LOG.info("Initializing TraceViewEditor - parsed tracing output: " + (System.currentTimeMillis() - start) + " ms");

            StudioNameResolver nameResolver = new StudioNameResolver(tracingOutput.getDictionary(), file);

            List<OpNode> data = new OpNodeTreeBuilder(prismContext).build(tracingOutput, nameResolver);
            if (data.size() == 1) {
                root = data.get(0);
            } else {
                throw new IllegalStateException("Unexpected # of OpNode objects: " + data.size());
            }
            LOG.info("Initializing TraceViewEditor - built op node tree: " + (System.currentTimeMillis() - start) + " ms");

        } catch (Exception ex) {
            MidPointService mm = MidPointService.get(project);
            mm.printToConsole(null, TraceViewEditor.class, "Couldn't load file", ex, ConsoleViewContentType.LOG_ERROR_OUTPUT);
            MidPointUtils.publishExceptionNotification(project, null, TraceViewEditor.class, NOTIFICATION_KEY, "Couldn't load file", ex);

            ex.printStackTrace();
            root = null;
        }

        if (root != null) {
            PresentationInitializer.initialize(root);
        }
        opTreePanel = new OpTreePanel(project, root);
        wrapper.setContent(opTreePanel);
    }

    public void applyOptions(Options options) {
        if (opTreePanel != null) {
            opTreePanel.applyOptions(options);
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
        if (opTreePanel != null) {
            opTreePanel.selectNotify();
        }
    }

    @Override
    public void deselectNotify() {
        if (opTreePanel != null) {
            opTreePanel.deselectNotify();
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
        return userDataHolder.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userDataHolder.putUserData(key, value);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @NotNull
    @Override
    public @Nullable VirtualFile getFile() {
        return file;
    }
}
