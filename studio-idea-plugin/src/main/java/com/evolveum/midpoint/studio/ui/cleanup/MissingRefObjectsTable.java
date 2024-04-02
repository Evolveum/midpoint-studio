package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;

public class MissingRefObjectsTable extends DefaultTreeTable<MissingRefObjectsTableModel> {

    public MissingRefObjectsTable() {
        super(new MissingRefObjectsTableModel());

        setupComponent();
    }

    private void setupComponent() {
        setDragEnabled(false);
        setRootVisible(false);
        
        this.tableHeader.setReorderingAllowed(false);
    }
}
