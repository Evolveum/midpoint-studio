package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;

public class MissingObjectsTable extends DefaultTreeTable<MissingObjectTableModel> {

    public MissingObjectsTable() {
        super(new MissingObjectTableModel());

        setupComponent();
    }

    private void setupComponent() {
        setDragEnabled(false);

        this.tableHeader.setReorderingAllowed(false);
    }
}
