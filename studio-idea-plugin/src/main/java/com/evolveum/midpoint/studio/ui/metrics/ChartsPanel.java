package com.evolveum.midpoint.studio.ui.metrics;

import com.intellij.util.ui.JBUI;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ChartsPanel extends JPanel {

    public ChartsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(JBUI.Borders.empty(5));

        add(createChart());
        add(createChart());
        add(createChart());
    }

    private JPanel createChart() {
        ChartPanel panel = new ChartPanel();
        panel.setBorder(JBUI.Borders.emptyBottom(15));

        return panel;
    }
}
