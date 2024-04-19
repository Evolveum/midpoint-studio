package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;

public class SynchronizationPanel extends BorderLayoutPanel {

    private final Project project;

    private SynchronizationTree tree;

    public SynchronizationPanel(Project project) {
        this.project = project;

        initLayout();
    }

    private void initLayout() {
        tree = new SynchronizationTree(new SynchronizationTreeModel());

        add(ScrollPaneFactory.createScrollPane(tree), BorderLayout.CENTER);
    }

    public SynchronizationTreeModel getModel() {
        return tree.getModel();
    }
}
