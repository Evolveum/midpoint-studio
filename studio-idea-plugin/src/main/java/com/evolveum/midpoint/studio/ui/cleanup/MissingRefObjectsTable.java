package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.SpeedSearchComparator;
import com.intellij.ui.TreeTableSpeedSearch;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MissingRefObjectsTable extends DefaultTreeTable<MissingRefObjectsTableModel> {

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

    private void setupPopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AnAction("Download") {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                getTableModel().markSelectedAction(MissingRefAction.DOWNLOAD);
            }
        });
        group.add(new AnAction("Ignore") {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                getTableModel().markSelectedAction(MissingRefAction.IGNORE);
            }
        });
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
