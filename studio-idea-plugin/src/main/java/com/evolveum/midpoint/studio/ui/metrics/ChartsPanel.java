package com.evolveum.midpoint.studio.ui.metrics;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ChartsPanel extends JPanel {

    public ChartsPanel() {
        JPanel panel = new BorderLayoutPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(5));

        panel.add(createChart());
        panel.add(createChart());
        panel.add(createChart());

        JBScrollPane pane = new JBScrollPane(panel);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(pane, BorderLayout.CENTER);
    }

    private JPanel createChart() {
        ChartPanel panel = new ChartPanel();
        panel.setBorder(JBUI.Borders.emptyBottom(10));

        return panel;
    }
}
