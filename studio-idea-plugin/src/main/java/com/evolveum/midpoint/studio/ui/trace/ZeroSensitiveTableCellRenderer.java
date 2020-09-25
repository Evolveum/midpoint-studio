package com.evolveum.midpoint.studio.ui.trace;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * TODO
 */
public class ZeroSensitiveTableCellRenderer extends DefaultTableCellRenderer {

    public static final TableCellRenderer INSTANCE = new ZeroSensitiveTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color foreground = getColor(value, isSelected);
        setForeground(foreground);
        return this;
    }

    private static Color getColor(Object value, boolean isSelected) {
        if (isZero(value)) {
            return JBUI.CurrentTheme.Label.disabledForeground(isSelected);
        } else {
            return JBUI.CurrentTheme.Label.foreground(isSelected);
        }
    }

    private static boolean isZero(Object value) {
        return value == null || value.toString().equals("0");
    }
}
