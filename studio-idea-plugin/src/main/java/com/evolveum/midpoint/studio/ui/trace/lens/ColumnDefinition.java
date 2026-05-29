package com.evolveum.midpoint.studio.ui.trace.lens;

import com.evolveum.midpoint.util.annotation.Experimental;

import javax.swing.table.TableCellRenderer;
import java.util.function.Function;

/**
 * Abstract definition of a column.
 * Currently used only for trace tree view.
 */
@Experimental
public interface ColumnDefinition<O> {

    String getName();

    Class<?> getType();

    int getSize();

    Function<O, Object> getValue();

    TableCellRenderer getTableCellRenderer();
}
