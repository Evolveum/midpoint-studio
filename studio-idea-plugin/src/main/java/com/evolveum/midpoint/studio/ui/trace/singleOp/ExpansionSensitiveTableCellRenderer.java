package com.evolveum.midpoint.studio.ui.trace.singleOp;

import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Renders an empty string for expanded rows (the tree column value is already shown in the tree cell),
 * and the actual value for collapsed/leaf rows.
 */
public class ExpansionSensitiveTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (table instanceof DefaultTreeTable<?, ?> treeTable && treeTable.getTree().isExpanded(row)) {
            return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
