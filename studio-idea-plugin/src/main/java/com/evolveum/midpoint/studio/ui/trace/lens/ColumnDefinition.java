package com.evolveum.midpoint.studio.ui.trace.lens;

import com.evolveum.midpoint.util.annotation.Experimental;

import javax.swing.table.TableCellRenderer;
import java.util.function.Function;

/**
 * Abstract definition of a column in the studio.
 * Currently used only for trace tree view.
 */
@Experimental
public interface ColumnDefinition<O> {

    String getName();

    int getSize();

    Function<O, String> getFormatter();

    TableCellRenderer getTableCellRenderer();
}
