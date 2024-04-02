package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.tree.TreeUtil;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MissingRefObjectsPanel extends JPanel {

    public MissingRefObjectsPanel(List<MissingRefObject> data) {
        setLayout(new BorderLayout());

        initLayout(data);
    }

    private void initLayout(List<MissingRefObject> data) {
        MissingRefObjectsTable table = new MissingRefObjectsTable();
        table.getTableModel().setData(data);
        TreeUtil.expandAll(table.getTree());

        add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER);

        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction(
                "Expand All",
                AllIcons.Actions.Expandall,
                e -> TreeUtil.expandAll(table.getTree()));
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction(
                "Collapse All",
                AllIcons.Actions.Collapseall,
                e -> TreeUtil.collapseAll(table.getTree(), -1));
        group.add(collapseAll);

        group.add(new Separator());

        AnAction remove = MidPointUtils.createAnAction(
                "Remove selected",
                AllIcons.General.Remove,
                e -> removeItems(table));
        group.add(remove);

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("MissingRefObjectsToolbar", group, true);
        toolbar.setTargetComponent(this);
        add(toolbar.getComponent(), BorderLayout.NORTH);
    }

    private void removeItems(MissingRefObjectsTable table) {
        int[] selected = table.getSelectedRows();

        table.getTableModel().removeNodes(selected);
    }
}
