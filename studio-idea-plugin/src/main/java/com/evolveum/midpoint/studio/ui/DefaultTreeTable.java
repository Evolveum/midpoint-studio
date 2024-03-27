package com.evolveum.midpoint.studio.ui;

import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;

public class DefaultTreeTable<M extends TreeTableModel> extends TreeTable {

    public DefaultTreeTable(M model) {
        super(model);
    }

    @Override
    public M getTableModel() {
        return (M) super.getTableModel();
    }
}
