package com.evolveum.midpoint.studio.impl.diff;

import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;

import java.util.List;

public class DeltaTreeTableModel extends DefaultTreeTableModel<ObjectDelta<?>> {

    private static final List<ColumnInfo> COLUMNS = List.of(
            new DefaultColumnInfo("", o -> true) {

                @Override
                public Class<?> getColumnClass() {
                    return Boolean.class;
                }
            },
            new DefaultColumnInfo("Path", o -> "asdf") {

                @Override
                public Class<?> getColumnClass() {
                    return TreeTableModel.class;
                }
            }
    );

    public DeltaTreeTableModel() {
        super(COLUMNS);
    }
}
