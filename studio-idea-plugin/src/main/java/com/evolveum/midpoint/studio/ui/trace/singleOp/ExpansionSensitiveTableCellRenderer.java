package com.evolveum.midpoint.studio.ui.trace.singleOp;

import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * TODO
 */
public class ExpansionSensitiveTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
        JXTreeTable treeTable = (JXTreeTable) table;
        if (treeTable.isExpanded(row)) {
            super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        } else {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        return this;
    }
}
