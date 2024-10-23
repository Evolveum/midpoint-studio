package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.UiAction;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.SpeedSearchComparator;
import com.intellij.ui.TreeTableSpeedSearch;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.swing.*;

public class MissingRefObjectsTable extends DefaultTreeTable<MissingRefObjectsTableModel> {

    private static final Icon ICON_OBJECT = MidPointIcons.Midpoint;

    private static final Icon ICON_REFERENCE = AllIcons.Nodes.Alias;

    public MissingRefObjectsTable() {
        super(new MissingRefObjectsTableModel());

        setupComponent();
    }

    private void setupComponent() {
        setDragEnabled(false);
        setRootVisible(false);

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        setupPopupMenu();
        setupSpeedSearch();

        this.tableHeader.setReorderingAllowed(false);
    }

    @Override
    protected Icon customizeTreeCellIcon(Object value) {
        if (!(value instanceof DefaultMutableTreeTableNode node)) {
            return super.customizeTreeCellIcon(value);
        }

        if (node.getUserObject() instanceof MissingRef) {
            return ICON_REFERENCE;
        }

        if (!(node.getUserObject() instanceof MissingRefNode refNode)) {
            return super.customizeTreeCellIcon(value);
        }

        if (refNode.getValue() instanceof MissingRefObject) {
            return ICON_OBJECT;
        }

        return super.customizeTreeCellIcon(value);
    }

    private void setupPopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new UiAction("Download", e -> getTableModel().markSelectedAction(MissingRefAction.DOWNLOAD)));
        group.add(new UiAction("Ignore", e -> getTableModel().markSelectedAction(MissingRefAction.IGNORE)));

        ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu("ObjectTreeTablePopupMenu", group);
        setComponentPopupMenu(menu.getComponent());
    }

    private void setupSpeedSearch() {
        // todo fix lambda
        TreeTableSpeedSearch search = TreeTableSpeedSearch.installOn(this, p -> p.toString());
        search.setComparator(new SpeedSearchComparator(false));
        search.setCanExpand(true);
    }
}
