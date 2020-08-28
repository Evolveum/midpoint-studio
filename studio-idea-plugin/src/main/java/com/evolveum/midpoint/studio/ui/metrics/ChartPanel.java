package com.evolveum.midpoint.studio.ui.metrics;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ChartPanel extends BorderLayoutPanel {

    private static final int CHART_HEIGHT = 200;

    public ChartPanel() {
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        initLayout();

        setOpaque(true);
        setBackground(Color.RED);
    }

    private void initLayout() {
        JBLabel label = new JBLabel("CPU Usage", UIUtil.ComponentStyle.LARGE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(JBUI.Fonts.label().biggerOn(2.0f).asBold());
        label.setBorder(JBUI.Borders.emptyBottom(5));

        add(label, BorderLayout.NORTH);

        final XYChart chart = new XYChartBuilder().width(600).height(CHART_HEIGHT).xAxisTitle("X").yAxisTitle("Y").build();

        // Customize Chart
        chart.getStyler().setChartBackgroundColor(JBUI.CurrentTheme.DefaultTabs.background());
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);

        // Series
        chart.addSeries("a", new double[] { 0, 3, 5, 7, 9 }, new double[] { -3, 5, 9, 6, 5 });
        chart.addSeries("b", new double[] { 0, 2, 4, 6, 9 }, new double[] { -1, 6, 4, 0, 4 });
        chart.addSeries("c", new double[] { 0, 1, 3, 8, 9 }, new double[] { -2, -1, 1, 0, 1 });

        XChartPanel chartPanel = new XChartPanel(chart);
        chartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartPanel.setMaximumSize(new Dimension(10000, 200));

        add(chartPanel, BorderLayout.CENTER);
    }
}
