package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel;

import javax.swing.*;

public class CreateConnector {

    private JPanel mainPanel;

    private final StatusPanel statusPanel = new StatusPanel();

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }
}
