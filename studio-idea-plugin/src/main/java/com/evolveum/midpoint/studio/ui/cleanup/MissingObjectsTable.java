package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.ui.DefaultTreeTable;

public class MissingObjectsTable extends DefaultTreeTable<MissingObjectTableModel> {

    public MissingObjectsTable() {
        super(new MissingObjectTableModel());

        setupComponent();
    }

    private void setupComponent() {
        setDragEnabled(false);

        this.tableHeader.setReorderingAllowed(false);

        this.columnModel.getColumn(0).setPreferredWidth(200);
        this.columnModel.getColumn(0).setMinWidth(50);

        this.columnModel.getColumn(1).setPreferredWidth(40);
        this.columnModel.getColumn(1).setMinWidth(40);
        this.columnModel.getColumn(1).setMaxWidth(40);

        this.columnModel.getColumn(1).setPreferredWidth(40);
        this.columnModel.getColumn(1).setMinWidth(40);
        this.columnModel.getColumn(1).setMaxWidth(40);
    }
}
