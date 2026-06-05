package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;

import javax.swing.*;

public class ConnectorIdentification {

    private final ConnectorGeneratorDataModel dataModel;

    private JPanel mainPanel;
    private JPanel header;
    private JPanel content;
    private JLabel text;
    private JTextPane subtext;
    private JPanel form;
    private JTextField artifactIdTextField;
    private JTextField displayNameTextField;
    private JTextArea descriptionTextArea;
    private JTextField versionTextField;
    private JTextField groupIdTextField;

    public ConnectorIdentification(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
        initComponents();
    }

    private void initComponents() {
        var connectorDevelopmentType = dataModel.connectorDevelopmentType;

        if (connectorDevelopmentType != null) {
            var connDevConnectorType = connectorDevelopmentType.getConnector();

            groupIdTextField.setText(connDevConnectorType != null &&
                    !connDevConnectorType.getGroupId().isBlank()
                    ? connDevConnectorType.getGroupId()
                    : "com.evolveum.polygon.community");
            artifactIdTextField.setText(connDevConnectorType != null &&
                    connDevConnectorType.getArtifactId() != null &&
                    !connDevConnectorType.getArtifactId().isBlank()
                    ? connDevConnectorType.getArtifactId()
                    : connectorDevelopmentType.getApplication().getApplicationName().getNorm());
            displayNameTextField.setText(connDevConnectorType != null
                    ? connDevConnectorType.getDisplayName().getNorm()
                    : null);
            versionTextField.setText(connDevConnectorType != null &&
                    connDevConnectorType.getVersion() != null &&
                    !connDevConnectorType.getVersion().isBlank()
                    ? connDevConnectorType.getVersion()
                    : "1.0");
            descriptionTextArea.setText(connDevConnectorType != null
                    ? connDevConnectorType.getDescription()
                    : null);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public JPanel getHeader() {
        return header;
    }

    public void setHeader(JPanel header) {
        this.header = header;
    }

    public JPanel getContent() {
        return content;
    }

    public void setContent(JPanel content) {
        this.content = content;
    }

    public JLabel getText() {
        return text;
    }

    public void setText(JLabel text) {
        this.text = text;
    }

    public JTextPane getSubtext() {
        return subtext;
    }

    public void setSubtext(JTextPane subtext) {
        this.subtext = subtext;
    }

    public JPanel getForm() {
        return form;
    }

    public void setForm(JPanel form) {
        this.form = form;
    }

    public JTextField getArtifactIdTextField() {
        return artifactIdTextField;
    }

    public void setArtifactIdTextField(JTextField artifactIdTextField) {
        this.artifactIdTextField = artifactIdTextField;
    }

    public JTextField getDisplayNameTextField() {
        return displayNameTextField;
    }

    public void setDisplayNameTextField(JTextField displayNameTextField) {
        this.displayNameTextField = displayNameTextField;
    }

    public JTextArea getDescriptionTextArea() {
        return descriptionTextArea;
    }

    public void setDescriptionTextArea(JTextArea descriptionTextArea) {
        this.descriptionTextArea = descriptionTextArea;
    }

    public JTextField getVersionTextField() {
        return versionTextField;
    }

    public void setVersionTextField(JTextField versionTextField) {
        this.versionTextField = versionTextField;
    }

    public JTextField getGroupIdTextField() {
        return groupIdTextField;
    }

    public void setGroupIdTextField(JTextField groupIdTextField) {
        this.groupIdTextField = groupIdTextField;
    }
}
