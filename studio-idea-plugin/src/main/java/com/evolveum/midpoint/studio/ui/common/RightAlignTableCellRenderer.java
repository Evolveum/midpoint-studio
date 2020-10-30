package com.evolveum.midpoint.studio.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * TODO
 */
public class RightAlignTableCellRenderer extends DefaultTableCellRenderer {

    public static final RightAlignTableCellRenderer INSTANCE = new RightAlignTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.setHorizontalAlignment(SwingConstants.RIGHT);
        return this;
    }
}
