package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class ConnectorIdentificationPanel extends JBPanel<ConnectorIdentificationPanel> implements WizardContent {

    private final ConnectorGeneratorDataModel dataModel;
    private Runnable updateDataModelByForm;

    public ConnectorIdentificationPanel(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
        setLayout(new BorderLayout());
        createConnectorIdentificationPanel();
    }

    private void createConnectorIdentificationPanel() {
        add(createTopBanner(), BorderLayout.NORTH);
        add(createConnectorIdentificationForm(), BorderLayout.CENTER);
    }

    private JBPanel<?> createTopBanner() {
        JBPanel<?> topPanel = new JBPanel<>();

        add(new JBLabel("Set basic information about the connector"));

        JBLabel description = new JBLabel("""
                On this panel you can enter the fundamental details that describe the connector,\s
                providing the necessary context before adding more specific configuration.
               """
        );
        description.setBorder(JBUI.Borders.emptyBottom(15));

        JBLabel header = new JBLabel("Identify the target application");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(header);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(description);

        return topPanel;
    }

    private JBPanel<?> createConnectorIdentificationForm() {
        var groupIdField = new JBTextField();
        var artifactIdField = new JBTextField();
        var displayNameField = new JBTextField();
        var versionField = new JBTextField();
        var descriptionArea = new JBTextArea(4, 40);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane descriptionScroll = new JBScrollPane(descriptionArea);

        if (dataModel.getConnectorDevelopmentType() != null) {
            var connectorDevelopmentType = dataModel.getConnectorDevelopmentType();
            var connDevConnectorType = connectorDevelopmentType.getConnector();

            groupIdField.setText(connDevConnectorType != null &&
                    !connDevConnectorType.getGroupId().isBlank()
                    ? connDevConnectorType.getGroupId()
                    : "com.evolveum.polygon.community");
            artifactIdField.setText(connDevConnectorType != null &&
                    connDevConnectorType.getArtifactId() != null &&
                    !connDevConnectorType.getArtifactId().isBlank()
                    ? connDevConnectorType.getArtifactId()
                    : connectorDevelopmentType.getApplication().getApplicationName().getNorm());
            displayNameField.setText(connDevConnectorType != null
                    ? connDevConnectorType.getDisplayName().getNorm()
                    : null);
            versionField.setText(connDevConnectorType != null &&
                    connDevConnectorType.getVersion() != null &&
                    !connDevConnectorType.getVersion().isBlank()
                    ? connDevConnectorType.getVersion()
                    : "1.0");
//            descriptionArea.setText(connDevConnectorType != null
//                    ? connDevConnectorType.getDescription().getNorm()
//                    : null);
        }

        updateDataModelByForm = () -> {
            if (dataModel.getConnectorDevelopmentType() != null) {
                var connDevConnectorType = new ConnDevConnectorType();

                connDevConnectorType.setGroupId(groupIdField.getText());
                connDevConnectorType.setArtifactId(artifactIdField.getText());
                connDevConnectorType.setDisplayName(new PolyStringType(displayNameField.getText()));
                connDevConnectorType.description(descriptionArea.getText());
                connDevConnectorType.setVersion(versionField.getText());

                dataModel.getConnectorDevelopmentType().setName(new PolyStringType(
                        groupIdField.getText() + ":" + artifactIdField.getText() + ":" + versionField.getText())
                );

                dataModel.getConnectorDevelopmentType().setConnector(connDevConnectorType);
            } else {
                // TODO error alert missing connector development type obj in dialogCtx
            }
        };

        var form = new JBPanel<>(new BorderLayout());

        form.add(FormBuilder.createFormBuilder()
                .addLabeledComponent("Group id *", groupIdField)
                .addLabeledComponent("Artifact id *", artifactIdField)
                .addLabeledComponent("Display name", displayNameField)
                .addLabeledComponent("Description", descriptionScroll)
                .addLabeledComponent("Version *", versionField)
                .getPanel());

        return form;
    }

    @Override
    public void beforeChangeAction() {
        // FIXME implementation validate formular values required / unrequired
        updateDataModelByForm.run();

        ProgressManager.getInstance().runProcessWithProgressSynchronously(
                () -> {
                    try {
                        EnvironmentService em = EnvironmentService.getInstance(dataModel.getProject());
                        Environment env = em.getSelected();
                        var client = new MidPointClient(dataModel.getProject(), env);
                        var connectorDevelopmentType = client.createConnectorDevelopmentType(
                                dataModel.getConnectorDevelopmentType());
                        dataModel.setConnectorDevelopmentType(connectorDevelopmentType);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                "Updating ConnectorDevelopmentType",
                true,
                dataModel.getProject()
        );
    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
