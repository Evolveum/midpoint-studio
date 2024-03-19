package com.evolveum.midpoint.studio.impl.diff;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.DiffUtil;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InitialObjectDiffProcessor<O extends ObjectType> {

    private final Project project;

    /**
     * Plain vanilla initial object from previous version of midPoint.
     */
    private final PrismObject<O> previousInitialObject;

    /**
     * Plain vanilla initial object from current (next) version of midPoint.
     */
    private final PrismObject<O> currentInitialObject;

    /**
     * Object that is currently being edited (stored in midPoint repository).
     */
    private final PrismObject<O> currentObject;

    private JComponent component;

    private DeltaTreeTable initialDeltaTable;

    private DeltaTreeTable currentDeltaTable;

    private Editor initialEditor;

    private Editor resultEditor;

    private Editor currentEditor;

    public InitialObjectDiffProcessor(
            @NotNull Project project,
            PrismObject<O> previousInitialObject,
            PrismObject<O> currentInitialObject,
            PrismObject<O> currentObject) {

        this.project = project;

        if (previousInitialObject == null && currentObject != null) {
            throw new IllegalArgumentException(
                    "Previous initial object must not be null since current must have been created from something");
        }

        this.previousInitialObject = previousInitialObject;
        this.currentInitialObject = currentInitialObject;
        this.currentObject = currentObject;

        init();

        initComponent();
    }

    private void init() {
        EditorFactory editorFactory = EditorFactory.getInstance();

        Document initialDocument = editorFactory.createDocument("initial");
        initialEditor = editorFactory.createViewer(initialDocument, project);

        Document resultDocument = editorFactory.createDocument("result");
        resultEditor = editorFactory.createViewer(resultDocument, project);

        Document currentDocument = editorFactory.createDocument("current");
        currentEditor = editorFactory.createViewer(currentDocument, project);
    }

    private void initComponent() {
        JBPanel root = new JBPanel(new BorderLayout());

        JComponent toolbar = initToolbar(root);
        root.add(toolbar, BorderLayout.NORTH);

        JComponent deltaViews = initDeltaViews();
        JComponent editors = initEditors();

        JBSplitter splitter = new OnePixelSplitter(true, "main", 0.33f);
        splitter.setFirstComponent(deltaViews);
        splitter.setSecondComponent(editors);

        root.add(splitter, BorderLayout.CENTER);

        this.component = root;
    }

    private JComponent initToolbar(JComponent parent) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AnAction("as") {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        });

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("toolbar", group, true);
        toolbar.setTargetComponent(parent);
        return toolbar.getComponent();
    }

    private JComponent initDeltaViews() {
        initialDeltaTable = new DeltaTreeTable(null);

        currentDeltaTable = new DeltaTreeTable(null);

        JBSplitter splitter = new OnePixelSplitter("deltas", 0.5f);
        splitter.setFirstComponent(new JBScrollPane(initialDeltaTable));
        splitter.setSecondComponent(new JBScrollPane(currentDeltaTable));

        return splitter;
    }

    private JComponent initEditors() {
        JBSplitter editorSplitter1 = new OnePixelSplitter("proportion_key1", 0.33f);
        editorSplitter1.setFirstComponent(new JBScrollPane(initialEditor.getComponent()));

        JBSplitter editorSplitter2 = new OnePixelSplitter("proportion_key2", 0.5f);
        editorSplitter2.setFirstComponent(new JBScrollPane(resultEditor.getComponent()));
        editorSplitter2.setSecondComponent(new JBScrollPane(currentEditor.getComponent()));

        editorSplitter1.setSecondComponent(editorSplitter2);

        return editorSplitter1;
    }

    private InitialObjectDiffRequest<O> process() {
        if (previousInitialObject == null && currentInitialObject != null) {
            // todo object was created by developers, no conflict - new initial object
        }

        ObjectDelta<O> initialChanges = DiffUtil.diff(previousInitialObject, currentInitialObject);

        if (previousInitialObject != null && currentObject == null && !initialChanges.isEmpty()) {
            // todo object was deleted by user, however developers have changed initial objects - conflict
        }

        ObjectDelta<O> currentChanges = DiffUtil.diff(previousInitialObject, currentObject);

        // todo check for conflicts in deltas

//        return new InitialObjectDiffRequest<>(initialChanges, currentChanges);
        return null;
    }

    public JComponent getComponent() {
        return component;
    }
}
