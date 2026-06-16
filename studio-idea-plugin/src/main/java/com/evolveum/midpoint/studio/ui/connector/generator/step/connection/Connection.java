package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel;

import javax.swing.*;

public class Connection {

    private ConnectorGeneratorDataModel dataModel;

    private final StatusPanel statusPanel = new StatusPanel();
    private JPanel mainPanel;

    public Connection(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}

