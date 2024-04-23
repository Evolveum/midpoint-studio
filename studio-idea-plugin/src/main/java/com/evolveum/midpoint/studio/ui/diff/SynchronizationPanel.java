package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.studio.ui.UiAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class SynchronizationPanel extends BorderLayoutPanel {

    private final Project project;

    private SynchronizationTree tree;

    public SynchronizationPanel(@NotNull Project project) {
        this.project = project;

        initLayout();
    }

    private void initLayout() {
        JComponent mainToolbar = initMainToolbar(this);
        add(mainToolbar, BorderLayout.NORTH);

        tree = new SynchronizationTree(project, new SynchronizationTreeModel());

        add(ScrollPaneFactory.createScrollPane(tree, true), BorderLayout.CENTER);
    }

    private JComponent initMainToolbar(JComponent parent) {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new UiAction(
                "Expand all", AllIcons.Actions.Expandall, e -> TreeUtil.expandAll(tree)));
        group.add(new UiAction(
                "Collapse all", AllIcons.Actions.Collapseall, e -> TreeUtil.collapseAll(tree, 1)));

        group.add(new Separator());

        group.add(new UiAction("Refresh", AllIcons.Actions.Refresh, e -> refreshPerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                boolean enabled = tree.getCheckedNodes(Object.class, null).length != 0;
                e.getPresentation().setEnabled(enabled);
            }
        });

        group.add(new UiAction("Save", AllIcons.General.GreenCheckmark, e -> savePerformed()) {

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                boolean enabled = tree.getCheckedNodes(Object.class, null).length != 0;
                e.getPresentation().setEnabled(enabled);
            }
        });

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("diff-panel-toolbar", group, true);
        toolbar.setTargetComponent(parent);

        return toolbar.getComponent();
    }

    public SynchronizationTreeModel getModel() {
        return (SynchronizationTreeModel) tree.getModel();
    }

    private void refreshPerformed() {
        // todo implement
    }

    private void savePerformed() {
        // todo implement
    }
}
