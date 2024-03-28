package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;

public class MissingObjectRefsTable extends DefaultTreeTable<MissingObjectRefsTableModel> {

    public MissingObjectRefsTable() {
        super(new MissingObjectRefsTableModel());

        setupComponent();
    }

    private void setupComponent() {
        setDragEnabled(false);

        this.tableHeader.setReorderingAllowed(false);
    }
}
