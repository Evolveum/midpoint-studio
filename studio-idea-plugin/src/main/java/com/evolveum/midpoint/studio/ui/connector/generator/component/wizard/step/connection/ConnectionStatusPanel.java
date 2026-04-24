package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DiscoverDocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class ConnectionStatusPanel extends JBPanel<DiscoverDocumentationPanel> implements WizardContent {

    private final JBLabel timerLabel;

    public ConnectionStatusPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 15, true, false));
        setBorder(JBUI.Borders.empty(100, 20));
        setBackground(UIUtil.getPanelBackground());

        AsyncProcessIcon progressIcon = new AsyncProcessIcon("ConnectionSearch");
        JBPanel<?> iconWrapper = new JBPanel<>(new FlowLayout(FlowLayout.CENTER));
        iconWrapper.setOpaque(false);
        iconWrapper.add(progressIcon);
        add(iconWrapper);

        JBLabel titleLabel = new JBLabel("Identifying Connection Possibilities...", SwingConstants.CENTER);
        titleLabel.setFont(JBUI.Fonts.label(20f).deriveFont(Font.PLAIN));
        titleLabel.setForeground(new Color(70, 130, 180));
        add(titleLabel);

        JBLabel descriptionLabel = new JBLabel("<html><center>This involves exploring and determining the available options for establishing a " +
                "connection, including supported protocols, authentication methods, and endpoints.</center></html>", SwingConstants.CENTER);
        descriptionLabel.setForeground(UIUtil.getContextHelpForeground());
        descriptionLabel.setPreferredSize(new Dimension(500, 60));
        add(descriptionLabel);

        timerLabel = new JBLabel("Elapsed time: 0s 0ms", SwingConstants.CENTER);
        timerLabel.setForeground(UIUtil.getInactiveTextColor());
        add(timerLabel);
    }

    public void updateTime(long millis) {
        long seconds = millis / 1000;
        long ms = millis % 1000;
        timerLabel.setText(String.format("Elapsed time: %ds %03dms", seconds, ms));
    }

    @Override
    public void beforeChangeAction() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
