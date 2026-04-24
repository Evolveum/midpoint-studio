package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends JBPanel<LoadingPanel> {

    private final JBLabel timerLabel;

    public LoadingPanel() {
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 15, true, false));
        setBorder(JBUI.Borders.empty(120, 20));
        setBackground(UIUtil.getPanelBackground());

        AsyncProcessIcon progressIcon = new AsyncProcessIcon("CreatingConnector");
        JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconWrapper.setOpaque(false);
        iconWrapper.add(progressIcon);
        add(iconWrapper);

        JBLabel titleLabel = new JBLabel("Creating Connector...", SwingConstants.CENTER);
        titleLabel.setFont(JBUI.Fonts.label(22f).deriveFont(Font.PLAIN));
        titleLabel.setForeground(new Color(70, 130, 180));
        add(titleLabel);

        JBLabel descriptionLabel = new JBLabel("<html><center>We use the connector's basic information to create a test instance for development and<br>testing purposes.</center></html>", SwingConstants.CENTER);
        descriptionLabel.setForeground(UIUtil.getContextHelpForeground());
        add(descriptionLabel);

        timerLabel = new JBLabel("Elapsed time: 450ms", SwingConstants.CENTER);
        timerLabel.setForeground(UIUtil.getInactiveTextColor());
        add(timerLabel);
    }

    public void updateElapsedTime(long startTimeMillis) {
        long elapsed = System.currentTimeMillis() - startTimeMillis;
        timerLabel.setText("Elapsed time: " + elapsed + "ms");
    }
}
