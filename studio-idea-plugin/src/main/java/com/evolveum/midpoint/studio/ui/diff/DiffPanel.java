package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.ui.UiAction;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public abstract class DiffPanel<O extends ObjectType> extends BorderLayoutPanel {

    private JBLabel label;

    private ObjectDeltaTree<O> deltaTree;

    private JBSplitter splitter;

    public DiffPanel() {
        initLayout();
    }

    public void setTargetName(@NotNull String targetName) {
        label.setText("Select changes to be applied to '" + targetName + "' object:");
    }

    public void setDelta(@NotNull ObjectDelta<O> delta) {
        ObjectDeltaTreeModel<O> model = getTreeModel();
        model.setData(delta);

        expandAllPerformed();
    }

    private void initLayout() {
        JComponent mainToolbar = initMainToolbar(this);
        add(mainToolbar, BorderLayout.NORTH);

        splitter = new JBSplitter(true, 0.5f);

        JBPanel<?> deltaPanel = createDeltaTablePanel();
        add(deltaPanel, BorderLayout.CENTER);

        splitter.setFirstComponent(deltaPanel);

        JComponent diffEditor = createTextDiff();
        splitter.setSecondComponent(diffEditor);
        add(splitter, BorderLayout.CENTER);
    }

    public void reloadDiffEditor() {
        JComponent diffEditor = createTextDiff();

        JComponent component = splitter.getSecondComponent();
        if (component instanceof Disposable disposable) {
            disposable.dispose();
        }

        splitter.setSecondComponent(diffEditor);
    }

    private JBPanel<?> createDeltaTablePanel() {
        deltaTree = new ObjectDeltaTree<>(new ObjectDeltaTreeModel<>());
        deltaTree.addTreeSelectionListener(e -> onTreeSelectionChanged(getSelectedNodes()));
        TreeUtil.expand(deltaTree, 2);

        JBPanel<?> treePanel = new BorderLayoutPanel();
        treePanel.setBorder(JBUI.Borders.customLineBottom(JBUI.CurrentTheme.Editor.BORDER_COLOR));

        label = new JBLabel();
        label.setBorder(JBUI.Borders.emptyLeft(12));
        treePanel.add(label, BorderLayout.NORTH);

        treePanel.add(ScrollPaneFactory.createScrollPane(this.deltaTree), BorderLayout.CENTER);

        return treePanel;
    }

    protected abstract JComponent createTextDiff();

    private JComponent initMainToolbar(JComponent parent) {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new UiAction("Expand All", AllIcons.Actions.Expandall, e -> expandAllPerformed()));
        group.add(new UiAction("Collapse All", AllIcons.Actions.Collapseall, e -> collapseAllPerformed()));

        List<AnAction> actions = createToolbarActions();
        actions.forEach(group::add);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("diff-panel-toolbar", group, true);
        toolbar.setTargetComponent(parent);

        return toolbar.getComponent();
    }

    protected void onTreeSelectionChanged(@NotNull List<DefaultMutableTreeNode> selected) {
        // intentionally left empty
    }

    protected @NotNull List<AnAction> createToolbarActions() {
        return List.of();
    }

    private void expandAllPerformed() {
        if (deltaTree == null) {
            return;
        }
        TreeUtil.expandAll(deltaTree);
    }

    private void collapseAllPerformed() {
        if (deltaTree == null) {
            return;
        }
        TreeUtil.collapseAll(deltaTree, 2);
    }

    public @NotNull List<DefaultMutableTreeNode> getSelectedNodes() {
        if (deltaTree == null) {
            return List.of();
        }

        DefaultMutableTreeNode[] selected = deltaTree.getSelectedNodes(DefaultMutableTreeNode.class, null);
        return Arrays.asList(selected);
    }

    public void removeNodes(@NotNull List<DefaultMutableTreeNode> nodes) {
        if (deltaTree == null) {
            return;
        }

        ObjectDeltaTreeModel<O> model = getTreeModel();
        nodes.forEach(model::removeNodeFromParent);
    }

    @SuppressWarnings("unchecked")
    private ObjectDeltaTreeModel<O> getTreeModel() {
        return (ObjectDeltaTreeModel<O>) deltaTree.getModel();
    }
}
