package com.evolveum.midpoint.studio.impl.diff;

import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.intellij.ui.treeStructure.treetable.TreeTable;

public class DeltaTreeTable extends TreeTable {

    public DeltaTreeTable(ObjectDelta<?> delta) {
        super(new DeltaTreeTableModel());

        setupComponent();
    }

    private void setupComponent() {
        setDragEnabled(false);

        this.tableHeader.setReorderingAllowed(false);

        this.columnModel.getColumn(0).setPreferredWidth(40);
        this.columnModel.getColumn(0).setMinWidth(40);
        this.columnModel.getColumn(0).setMaxWidth(40);

        this.columnModel.getColumn(1).setPreferredWidth(200);
        this.columnModel.getColumn(1).setMinWidth(50);
    }
}
