package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;

import javax.swing.*;

public class AuthScriptsConnector {

    private final ConnectorGeneratorDataModel dataModel;

    private JPanel mainPanel;
    private JPanel content;
    private JPanel header;
    private JLabel text;
    private JTextPane subtext;
    private JPanel formPanel;
    private JPanel editorWrapper;
    private JButton regenerateScriptBtn;
    private SmartEditorComponent smartEditorComponent;

    public AuthScriptsConnector(ConnectorGeneratorDataModel dataModel, Project project) {
        this.dataModel = dataModel;
        initComponents(project);
    }

    private void initComponents(Project project) {

        var initScript = dataModel.connectorDevelopmentType.getConnector().getAuthenticationScript().getContent();

        smartEditorComponent = new SmartEditorComponent(
                project,
                PlainTextLanguage.INSTANCE,
                initScript == null ? "" : initScript);
        getEditorWrapper().add(smartEditorComponent);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JPanel getContent() {
        return content;
    }

    public JPanel getHeader() {
        return header;
    }

    public JLabel getText() {
        return text;
    }

    public JTextPane getSubtext() {
        return subtext;
    }

    public JPanel getFormPanel() {
        return formPanel;
    }

    public JPanel getEditorWrapper() {
        return editorWrapper;
    }

    public JButton getRegenerateScriptBtn() {
        return regenerateScriptBtn;
    }

    public SmartEditorComponent getSmartEditorComponent() {
        return smartEditorComponent;
    }
}
