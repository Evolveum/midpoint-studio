package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.ui.trace.lens.ColumnDefinition;

import javax.swing.table.TableCellRenderer;
import java.util.function.Function;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TreeTableColumnDefinition<R, V> {

    private final String header;

    private final int size;

    private final Function<R, V> value;

    private TableCellRenderer tableCellRenderer;

    ColumnDefinition<R> originalColumnDefinition;

    private boolean visible = true;

    public TreeTableColumnDefinition(String header, int size, Function<R, V> value) {
        this(header, size, value, null);
    }

    public TreeTableColumnDefinition(String header, int size, Function<R, V> value, TableCellRenderer tableCellRenderer) {
        this.header = header;
        this.size = size;
        this.value = value;
        this.tableCellRenderer = tableCellRenderer;
    }

    public TreeTableColumnDefinition(ColumnDefinition<R> columnDefinition) {
        this.header = columnDefinition.getName();
        this.size = columnDefinition.getSize();
        this.value = v -> (V) columnDefinition.getFormatter().apply(v);
        this.originalColumnDefinition = columnDefinition;
        this.tableCellRenderer = columnDefinition.getTableCellRenderer();
    }

    public TreeTableColumnDefinition<R, V> tableCellRenderer(TableCellRenderer tableCellRenderer) {
        this.tableCellRenderer = tableCellRenderer;
        return this;
    }

    public String getHeader() {
        return header;
    }

    public int getSize() {
        return size;
    }

    public Function<R, V> getValue() {
        return value;
    }

    public TableCellRenderer getTableCellRenderer() {
        return tableCellRenderer;
    }

    public ColumnDefinition<R> getOriginalColumnDefinition() {
        return originalColumnDefinition;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return originalColumnDefinition != null ? String.valueOf(originalColumnDefinition) : "COL:" + header;
    }
}
