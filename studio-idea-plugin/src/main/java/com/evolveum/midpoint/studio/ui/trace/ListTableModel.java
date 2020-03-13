package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ListTableModel<T> extends AbstractTableModel {

    private List<TreeTableColumnDefinition<T, ?>> columns;
    private List<T> data;

    public ListTableModel(List<TreeTableColumnDefinition<T, ?>> columns, List<T> data) {
        this.columns = columns;
        this.data = data;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }

    @Override
    public Object getValueAt(int row, int column) {
        T object = data.get(row);

        return columns.get(column).getValue().apply(object);
    }
}
