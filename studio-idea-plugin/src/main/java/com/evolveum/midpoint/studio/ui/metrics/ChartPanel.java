package com.evolveum.midpoint.studio.ui.metrics;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Date;
import java.util.TimerTask;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ChartPanel extends JPanel {

    private JPanel root;

    private XChartPanel chart;

    public ChartPanel() {
        super(new BorderLayout());

        add(root, BorderLayout.CENTER);
    }

    private void createUIComponents() {
        System.out.println("create");
        XYChart xchart = new XYChart(800, 600);
        xchart.setXAxisTitle("Time");
        xchart.setYAxisTitle("Usage");
        xchart.getStyler().setDatePattern("HH:mm:ss");
        xchart.getStyler().setYAxisMin(0.0d);
        xchart.getStyler().setYAxisMin(1.0d);
        xchart.getStyler().setChartBackgroundColor(Color.ORANGE);
        xchart.getStyler().setYAxisDecimalPattern("#0.00");

        String seriesName = "Node";
        XYSeries series = xchart.addSeries(seriesName, Arrays.<Date>asList(new Date()), Arrays.<Double>asList(0.0d), null);
        chart = new XChartPanel(xchart);

//        WebClient client = WebClient.create("http://localhost:8080/midpoint/actuator/metrics/system.cpu.usage",
//                Arrays.asList(new SimpleTypeJsonProvider()), "administrator", "5ecr3t", null);

        TimerTask chartUpdaterTask = new TimerTask() {

            @Override
            public void run() {
                try {
//                    Map map = client.get(Map.class);
//                    System.out.println(map);
                    System.out.println("asdf");
                    xchart.updateXYSeries(seriesName, Arrays.<Date>asList(new Date()), Arrays.<Double>asList(Math.random()), null);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(chartUpdaterTask, 0, 1000);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());
        frame.add(new ChartPanel(), BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
