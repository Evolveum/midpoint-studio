package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.AiAlertPanel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class BaseUrlConnector {

    private ConnectorGeneratorDataModel dataModel;

    private JPanel mainPanel;
    private JPanel content;
    private JPanel formPanel;
    private JPanel header;
    private JLabel text;
    private JTextPane subtext;
    private JPanel aiAlert;
    private JTextField restBaseAddressField;

    public BaseUrlConnector(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
        initComponents();
    }

    private void initComponents() {
        if (dataModel.connectorDevelopmentType != null) {
            var application = dataModel.connectorDevelopmentType.getApplication();

            if (application != null) {
                getRestBaseAddressField().setText(application.getBaseApiEndpoint());
            }
        }
    }


    private void createUIComponents() {
        aiAlert = new AiAlertPanel(new BorderLayout(), 15, "Endpoints identified", """
                A likely URLs for the test purposes from your documentation were detected
                """);
    }

    public JTextField getRestBaseAddressField() {
        return restBaseAddressField;
    }

    public void setRestBaseAddressField(JTextField restBaseAddressField) {
        this.restBaseAddressField = restBaseAddressField;
    }

    public JTextPane getSubtext() {
        return subtext;
    }

    public void setSubtext(JTextPane subtext) {
        this.subtext = subtext;
    }

    public JLabel getText() {
        return text;
    }

    public void setText(JLabel text) {
        this.text = text;
    }

    public JPanel getHeader() {
        return header;
    }

    public void setHeader(JPanel header) {
        this.header = header;
    }

    public JPanel getFormPanel() {
        return formPanel;
    }

    public void setFormPanel(JPanel formPanel) {
        this.formPanel = formPanel;
    }

    public JPanel getContent() {
        return content;
    }

    public void setContent(JPanel content) {
        this.content = content;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public ConnectorGeneratorDataModel getDataModel() {
        return dataModel;
    }

    public void setDataModel(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
    }
}
