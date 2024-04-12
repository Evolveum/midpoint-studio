package com.evolveum.midpoint.studio.impl.diff;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.DiffUtil;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.ui.UiAction;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.icons.AllIcons;
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
     * E.g. Plain vanilla initial object from previous version of midPoint.
     */
    private final PrismObject<O> baseObject;

    /**
     * E.g. Plain vanilla initial object from current (next) version of midPoint.
     */
    private final PrismObject<O> leftObject;

    /**
     * E.g. Object that is currently being edited (stored in midPoint repository).
     */
    private final PrismObject<O> rightObject;

    private JComponent component;

    private DeltaTreeTable leftDeltaTable;

    private DeltaTreeTable rightDeltaTable;

    private Editor leftEditor;

    private Editor baseEditor;

    private Editor rightEditor;

    public InitialObjectDiffProcessor(
            @NotNull Project project,
            PrismObject<O> baseObject,
            PrismObject<O> leftObject,
            PrismObject<O> rightObject) {

        this.project = project;

        if (baseObject == null && rightObject != null) {
            throw new IllegalArgumentException(
                    "Previous initial object must not be null since current must have been created from something");
        }

        this.baseObject = baseObject;
        this.leftObject = leftObject;
        this.rightObject = rightObject;

        init();

        initComponent();
    }

    private void init() {
        EditorFactory editorFactory = EditorFactory.getInstance();

        Document leftDocument = editorFactory.createDocument("initial");
        leftEditor = editorFactory.createViewer(leftDocument, project);

        Document baseDocument = editorFactory.createDocument("result");
        baseEditor = editorFactory.createViewer(baseDocument, project);

        Document rightDocument = editorFactory.createDocument("current");
        rightEditor = editorFactory.createViewer(rightDocument, project);
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
        group.add(new UiAction("Apply non-conflicting from left", AllIcons.Chooser.Right, e -> {
        }));
        group.add(new UiAction("Apply non-conflicting from right", AllIcons.Chooser.Left, e -> {
        }));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("toolbar", group, true);
        toolbar.setTargetComponent(parent);
        return toolbar.getComponent();
    }

    private JComponent initDeltaViews() {
        leftDeltaTable = new DeltaTreeTable(null);

        rightDeltaTable = new DeltaTreeTable(null);

        JBSplitter splitter = new OnePixelSplitter("deltas", 0.5f);
        splitter.setFirstComponent(new JBScrollPane(leftDeltaTable));
        splitter.setSecondComponent(new JBScrollPane(rightDeltaTable));

        return splitter;
    }

    private JComponent initEditors() {
        JBSplitter editorSplitter1 = new OnePixelSplitter("proportion_key1", 0.33f);
        editorSplitter1.setFirstComponent(new JBScrollPane(leftEditor.getComponent()));

        JBSplitter editorSplitter2 = new OnePixelSplitter("proportion_key2", 0.5f);
        editorSplitter2.setFirstComponent(new JBScrollPane(baseEditor.getComponent()));
        editorSplitter2.setSecondComponent(new JBScrollPane(rightEditor.getComponent()));

        editorSplitter1.setSecondComponent(editorSplitter2);

        return editorSplitter1;
    }

    private InitialObjectDiffRequest<O> process() {
        if (baseObject == null && leftObject != null) {
            // todo object was created by developers, no conflict - new initial object
        }

        ObjectDelta<O> initialChanges = DiffUtil.diff(baseObject, leftObject);

        if (baseObject != null && rightObject == null && !initialChanges.isEmpty()) {
            // todo object was deleted by user, however developers have changed initial objects - conflict
        }

        ObjectDelta<O> currentChanges = DiffUtil.diff(baseObject, rightObject);

        // todo check for conflicts in deltas

//        return new InitialObjectDiffRequest<>(initialChanges, currentChanges);
        return null;
    }

    public JComponent getComponent() {
        return component;
    }
}
