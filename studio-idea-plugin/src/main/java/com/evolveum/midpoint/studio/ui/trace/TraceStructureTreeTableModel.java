package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.OpNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceStructureTreeTableModel extends DefaultTreeTableModel {

    private List<TreeTableColumnDefinition> columns;

    private List<OpNode> data;

    public TraceStructureTreeTableModel(List<TreeTableColumnDefinition> columns, List<OpNode> data) {
        this.columns = columns;
        this.data = data;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        return null;    // todo
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }
}
