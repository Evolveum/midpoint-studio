package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class ConnectorIdentificationPanel extends JBPanel<ConnectorIdentificationPanel> implements WizardContent {

    private final ConnectorGeneratorDialogContext dialogContext;
    private Runnable updateDialogContextByFrom;

    public ConnectorIdentificationPanel(ConnectorGeneratorDialogContext dialogContext) {
        this.dialogContext = dialogContext;
        add(createTopBanner());
        add(createConnectorIdentificationForm());
    }

    private JPanel createTopBanner() {
        JPanel topPanel = new JBPanel<>();

        add(new JLabel("Set Basic Information About the Connector"));

        JLabel description = new JLabel(
                "On this panel you can enter the fundamental details that describe the connector, providing the necessary context before adding more specific configuration."
        );
        description.setBorder(JBUI.Borders.emptyBottom(15));

        JLabel header = new JLabel("Identify the Target Application");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(header);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(description);

        return topPanel;
    }

    private JPanel createConnectorIdentificationForm() {
        var groupIdField = new JBTextField();
        var artifactIdField = new JBTextField();
        var displayNameField = new JBTextField();
        var versionField = new JBTextField();
        var descriptionArea = new JBTextArea(4, 40);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JBScrollPane(descriptionArea);

        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Group id *", groupIdField)
                .addLabeledComponent("Artifact id *", artifactIdField)
                .addLabeledComponent("Display name", displayNameField)
                .addLabeledComponent("Description", descriptionScroll)
                .addLabeledComponent("Version *", versionField)
                .getPanel();

        updateDialogContextByFrom = () -> {
            if (dialogContext.getConnectorDevelopmentType() != null) {
                var connDevConnectorType = new ConnDevConnectorType();

                connDevConnectorType.setGroupId(groupIdField.getText());
                connDevConnectorType.setArtifactId(artifactIdField.getText());
                connDevConnectorType.setDisplayName(new PolyStringType(displayNameField.getText()));
                connDevConnectorType.description(descriptionArea.getText());
                connDevConnectorType.setVersion(versionField.getText());

                dialogContext.getConnectorDevelopmentType().setName(new PolyStringType(
                        groupIdField.getText() + ":" + artifactIdField.getText() + ":" + versionField.getText())
                );

                dialogContext.getConnectorDevelopmentType().setConnector(connDevConnectorType);
            }
        };

        return formPanel;
    }

    @Override
    public void afterChangeAction() {
        updateDialogContextByFrom.run();

        ProgressManager.getInstance().runProcessWithProgressSynchronously(
                () -> {
                    try {
                        EnvironmentService em = EnvironmentService.getInstance(dialogContext.getProject());
                        Environment env = em.getSelected();
                        var client = new MidPointClient(dialogContext.getProject(), env);
                        var connectorDevelopmentType = client.createConnectorDevelopmentType(
                                dialogContext.getConnectorDevelopmentType());
                        dialogContext.setConnectorDevelopmentType(connectorDevelopmentType);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                "Updating ConnectorDevelopmentType",
                true,
                dialogContext.getProject()
        );
    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
