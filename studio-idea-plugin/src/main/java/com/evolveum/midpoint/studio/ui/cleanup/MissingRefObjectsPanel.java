package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MissingRefObjectsPanel extends JPanel {

    private MissingRefObjectsTable table;

    public MissingRefObjectsPanel() {
        setLayout(new BorderLayout());

        initLayout();
    }

    public List<MissingRefObject> getData() {
        return table.getTableModel().getData();
    }

    public void setData(List<MissingRefObject> data) {
        table.getTableModel().setData(data);

        TreeUtil.expandAll(table.getTree());
    }

    private void initLayout() {
        table = new MissingRefObjectsTable();

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
