package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceStructureTreeTableModel<T> extends AbstractTreeTableModel {

    private List<TreeTableColumnDefinition<T, ?>> columns;

    private List<T> data;

    public TraceStructureTreeTableModel(List<TreeTableColumnDefinition<T, ?>> columns, List<T> data) {
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
    public Object getChild(Object parent, int index) {
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == null) {
            return data.size();
        }

        return 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return 0;
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }
}
