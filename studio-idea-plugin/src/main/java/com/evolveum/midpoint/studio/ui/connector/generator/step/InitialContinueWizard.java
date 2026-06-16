package com.evolveum.midpoint.studio.ui.connector.generator.step;

import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel;

import javax.swing.*;

public class InitialContinueWizard {

    private final StatusPanel statusPanel = new StatusPanel();
    private JPanel mainPanel;
    private JPanel content;
    private JLabel text;
    private JTextPane subtext;
    private JPanel header;

    public InitialContinueWizard() {
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
