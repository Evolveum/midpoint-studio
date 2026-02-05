package com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.action;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ActionsRenderer extends ActionPanel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        setBackground(isSelected
                ? table.getSelectionBackground()
                : table.getBackground());


        return this;
    }
}