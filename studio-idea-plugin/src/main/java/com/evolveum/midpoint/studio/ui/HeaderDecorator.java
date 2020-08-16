package com.evolveum.midpoint.studio.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class HeaderDecorator extends BorderLayoutPanel {

    private JComponent component;

    public HeaderDecorator(@NotNull String text, @NotNull JComponent component) {
        add(createHeader(text), BorderLayout.NORTH);
        add(component, BorderLayout.CENTER);
    }

    protected JComponent createHeader(String text) {
        JBLabel header = new JBLabel(text);
        header.setOpaque(true);
        header.setVerticalTextPosition(SwingConstants.CENTER);
        header.setForeground(JBColor.foreground());
        header.setBackground(JBUI.CurrentTheme.ToolWindow.headerActiveBackground());
        header.setFont(JBUI.CurrentTheme.ToolWindow.headerFont());

        int padding = JBUI.CurrentTheme.ToolWindow.tabVerticalPadding();
        header.setBorder(JBUI.Borders.empty(padding, 7, padding, 0));

        return header;
    }
}
