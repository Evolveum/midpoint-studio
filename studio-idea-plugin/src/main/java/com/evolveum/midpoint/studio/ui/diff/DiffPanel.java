package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.ui.UiAction;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public abstract class DiffPanel<O extends ObjectType> extends JBPanel {

    private final Project project;

    private ObjectDeltaTree<O> deltaTree;

    private String targetName;

    private ObjectDelta<O> delta;

    public DiffPanel(@NotNull Project project, String targetName, ObjectDelta<O> delta) {
        super(new BorderLayout());

        this.project = project;
        this.targetName = targetName != null ? targetName : "target";
        this.delta = delta;

        initLayout();
    }

    private void initLayout() {
        JComponent mainToolbar = initMainToolbar(this);
        add(mainToolbar, BorderLayout.NORTH);

        JBSplitter splitter = new JBSplitter(true, 0.5f);

        JBPanel deltaPanel = createDeltaTablePanel();
        splitter.setFirstComponent(deltaPanel);

        JComponent diffEditor = createTextDiff();
        splitter.setSecondComponent(diffEditor);
        add(splitter, BorderLayout.CENTER);
    }

    private JBPanel createDeltaTablePanel() {
        deltaTree = new ObjectDeltaTree(new ObjectDeltaTreeModel(delta));
        deltaTree.addTreeSelectionListener(e -> onTreeSelectionChanged(getSelectedNodes()));
        TreeUtil.expand(deltaTree, 2);

        JBPanel treePanel = new JBPanel(new BorderLayout());
        treePanel.setBorder(JBUI.Borders.customLineBottom(JBUI.CurrentTheme.Editor.BORDER_COLOR));

        JBLabel label = new JBLabel("Select changes to be applied to '" + targetName + "' object:");
        label.setBorder(JBUI.Borders.emptyLeft(12));
        treePanel.add(label, BorderLayout.NORTH); // todo fix

        treePanel.add(ScrollPaneFactory.createScrollPane(this.deltaTree), BorderLayout.CENTER);

        return treePanel;
    }

    protected abstract JComponent createTextDiff();

    private JComponent initMainToolbar(JComponent parent) {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new UiAction("Expand all", AllIcons.Actions.Expandall, e -> expandAllPerformed()));
        group.add(new UiAction("Collapse all", AllIcons.Actions.Collapseall, e -> collapseAllPerformed()));

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

        ObjectDeltaTreeModel<O> model = (ObjectDeltaTreeModel<O>) deltaTree.getModel();
        nodes.forEach(n -> model.removeNodeFromParent(n));
    }
}
