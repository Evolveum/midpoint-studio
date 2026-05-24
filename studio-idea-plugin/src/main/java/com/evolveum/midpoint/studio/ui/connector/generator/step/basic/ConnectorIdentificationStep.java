package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ConnectorIdentificationStep extends StepAdapter {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel;
    private StepStateBadge.State state;
    private final JBPanel<?> panel = new JBPanel<>();

    private JBTextField groupIdField;
    private JBTextField artifactIdField;
    private JBTextField displayNameField;
    private JBTextField versionField;
    private JBTextArea descriptionArea;

    private boolean initialized = false;

    public ConnectorIdentificationStep(
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        this.client = client;
        this.dataModel = dataModel;
        this.state = state;
        panel.setName("Connector identification");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;
            panel.add(crateStepContent());
        }

        super._init();
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {

        if (groupIdField.getText() == null ||
                groupIdField.getText().isBlank()) {
            throw new CommitStepException("Group ID is required field");
        }

        if (artifactIdField.getText() == null ||
                artifactIdField.getText().isBlank()) {
            throw new CommitStepException("Artifact Id is required field");
        }

        if (versionField.getText() == null ||
                versionField.getText().isBlank()) {
            throw new CommitStepException("Version is required field");
        }

        try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                    () -> {
                        var connectorDevelopmentType = dataModel.connectorDevelopmentType;
                        connectorDevelopmentType.setConnector(getConnDevConnectorType());
//                        dataModel.connectorDevelopmentType = client.upsertConnectorDevelopmentType(connectorDevelopmentType);
                    },
                    "Update ConnectorDevelopmentType",
                    true,
                    client.getProject()
            );
        } catch (Exception e) {
            throw new CommitStepException(e.getMessage());
        }

        super._commit(finishChosen);
    }

    private @NotNull ConnDevConnectorType getConnDevConnectorType() {
        var connDevConnectorType = new ConnDevConnectorType();
        connDevConnectorType.setGroupId(groupIdField.getText());
        connDevConnectorType.setArtifactId(artifactIdField.getText());
        connDevConnectorType.setDisplayName(new PolyStringType(displayNameField.getText()));
        connDevConnectorType.setDescription(descriptionArea.getText());
        connDevConnectorType.setVersion(versionField.getText());
        return connDevConnectorType;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    public StepStateBadge.State getState() {
        return state;
    }

    public void setState(StepStateBadge.State state) {
        this.state = state;
    }

    private JBPanel<?> crateStepContent() {
        var mainPanel = new JBPanel<>();

        JBLabel text = new JBLabel("Set Basic Information About the Connector");
        text.setFont(text.getFont().deriveFont(Font.BOLD, 18f));

        JBLabel subText = new JBLabel("""
                On this panel you can enter the fundamental details that describe the connector,\s
                providing the necessary context before adding more specific configuration.
                """
        );
        subText.setBorder(JBUI.Borders.emptyBottom(15));

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(text);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subText);
        mainPanel.add(createConnectorIdentificationForm());

        return mainPanel;
    }

    private JBPanel<?> createConnectorIdentificationForm() {
        groupIdField = new JBTextField();
        artifactIdField = new JBTextField();
        displayNameField = new JBTextField();
        versionField = new JBTextField();
        descriptionArea = new JBTextArea(4, 40);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane descriptionScroll = new JBScrollPane(descriptionArea);

        var form = new JBPanel<>(new BorderLayout());

        var connectorDevelopmentType = dataModel.connectorDevelopmentType;

        if (connectorDevelopmentType != null) {
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
            descriptionArea.setText(connDevConnectorType != null
                    ? connDevConnectorType.getDescription()
                    : null);
        }

        form.add(FormBuilder.createFormBuilder()
                .addLabeledComponent("Group id *", groupIdField)
                .addLabeledComponent("Artifact id *", artifactIdField)
                .addLabeledComponent("Display name", displayNameField)
                .addLabeledComponent("Description", descriptionScroll)
                .addLabeledComponent("Version *", versionField)
                .getPanel());

        return form;
    }
}
