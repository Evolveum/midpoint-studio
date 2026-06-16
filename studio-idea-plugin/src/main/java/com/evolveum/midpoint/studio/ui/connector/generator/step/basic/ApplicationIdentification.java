package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.intellij.util.ui.JBUI;

import javax.swing.*;

public class ApplicationIdentification {

    private final String COMBO_BOX_ITEM_UNDEFINED = "Undefined";

    private final ConnectorGeneratorDataModel dataModel;

    private JPanel mainPanel;
    private JTextField applicationNameField;
    private JTextArea descriptionArea;
    private JComboBox<String> integrationTypeCombo;
    private JTextField versionField;
    private JComboBox<String> deploymentTypeCombo;
    private JLabel text;
    private JTextPane subtext;
    private JPanel content;
    private JPanel header;
    private JPanel formPanel;
    private JScrollPane descriptionAreaScrollPanel;

    public ApplicationIdentification(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
        initComponents();
    }

    private void initComponents() {
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(UIManager.getColor("TextField.background"));
        descriptionArea.setForeground(UIManager.getColor("TextField.foreground"));
        descriptionArea.setBorder(JBUI.Borders.empty(5));

        integrationTypeCombo.addItem(COMBO_BOX_ITEM_UNDEFINED);

        for (ConnDevIntegrationType type : ConnDevIntegrationType.values()) {
            integrationTypeCombo.addItem(type.name());
        }

        deploymentTypeCombo.addItem(COMBO_BOX_ITEM_UNDEFINED);

        for (ConnDevDeploymentType type : ConnDevDeploymentType.values()) {
            deploymentTypeCombo.addItem(type.name());
        }

        if (dataModel.connectorDevelopmentType != null) {
            var application = dataModel.connectorDevelopmentType.getApplication();

            if (application != null) {
                applicationNameField.setText(application.getApplicationName().getOrig());
                versionField.setText(application.getVersion());
                descriptionArea.setText(application.getDescription());
                integrationTypeCombo.setSelectedItem(application.getIntegrationType() == null
                        ? COMBO_BOX_ITEM_UNDEFINED
                        : application.getIntegrationType().name());
                deploymentTypeCombo.setSelectedItem(
                        application.getDeploymentType() == null
                                ? COMBO_BOX_ITEM_UNDEFINED
                                : application.getDeploymentType().name());
            }
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getApplicationNameField() {
        return applicationNameField;
    }

    public JTextArea getDescriptionArea() {
        return descriptionArea;
    }

    public JComboBox<String> getIntegrationTypeCombo() {
        return integrationTypeCombo;
    }

    public JTextField getVersionField() {
        return versionField;
    }

    public JComboBox<String> getDeploymentTypeCombo() {
        return deploymentTypeCombo;
    }
}
