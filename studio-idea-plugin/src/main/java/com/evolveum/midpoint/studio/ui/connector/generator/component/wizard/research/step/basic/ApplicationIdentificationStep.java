package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.ConnectorGeneratorWizardData;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.StepStateBadge;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class ApplicationIdentificationStep extends StepAdapter {

    private final MidPointClient client;
    private final ConnectorGeneratorWizardData dataModel;
    private StepStateBadge.State state;
    private final JBPanel<?> panel = new JBPanel<>();

    private JBTextField applicationNameField;
    private JBTextField versionField;
    private JBTextArea descriptionArea;
    private ComboBox<String> integrationTypeCombo;
    private ComboBox<String> deploymentTypeCombo;

    private final String COMBO_BOX_ITEM_UNDEFINED = "Undefined";

    private boolean initialized = false;

    public ApplicationIdentificationStep(
            MidPointClient client,
            ConnectorGeneratorWizardData dataModel,
            StepStateBadge.State state
    ) {
        this.client = client;
        this.dataModel = dataModel;
        this.state = state;
        panel.setName("Application Identification");
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
        var connection = client.testConnection();

        if (!connection.success()) {
            throw new CommitStepException(connection.exception().getMessage());
        }

        if (applicationNameField.getText() == null ||
                applicationNameField.getText().isBlank()) {
            throw new CommitStepException("Field Application Name is required");
        }

        if (integrationTypeCombo.getSelectedItem() == null ||
                integrationTypeCombo.getSelectedItem().equals(COMBO_BOX_ITEM_UNDEFINED) ) {
            throw new CommitStepException("Field Integration Type is required");
        }

        try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {

                var connectorDevelopmentType = new ConnectorDevelopmentType();
                connectorDevelopmentType.setApplication(getConnDevApplicationInfoType());
                connectorDevelopmentType.setName(
                        connectorDevelopmentType.getApplication().getApplicationName()
                );

                try {
                    dataModel.connectorDevelopmentType =
                            client.upsert(connectorDevelopmentType.asPrismObject(), null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, "Create ConnectorDevelopmentType object", true, client.getProject());

            if (dataModel.connectorDevelopmentType == null) {
                throw new CommitStepException(
                        "Failed to create ConnectorDevelopmentType object"
                );
            }

        } catch (Exception ex) {
            throw new CommitStepException(ex.getMessage());
        }

        super._commit(finishChosen);
    }

    private @NotNull ConnDevApplicationInfoType getConnDevApplicationInfoType() {
        var connDevApplicationInfoType = new ConnDevApplicationInfoType();
        connDevApplicationInfoType.setApplicationName(new PolyStringType(applicationNameField.getText()));
        connDevApplicationInfoType.setVersion(versionField.getText());
        connDevApplicationInfoType.setDescription(descriptionArea.getText());
        connDevApplicationInfoType.setIntegrationType(
                integrationTypeCombo.getSelectedItem() instanceof String s && !s.equals(COMBO_BOX_ITEM_UNDEFINED)
                        ? ConnDevIntegrationType.fromValue(s.toLowerCase())
                        : null
        );
        connDevApplicationInfoType.setDeploymentType(
                deploymentTypeCombo.getSelectedItem() instanceof String s && !s.equals(COMBO_BOX_ITEM_UNDEFINED)
                        ? ConnDevDeploymentType.fromValue(s.toLowerCase())
                        : null
        );
        return connDevApplicationInfoType;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    public StepStateBadge.State getState() {
        return state;
    }

    public void setState(StepStateBadge.State state) {
        this.state = state;
    }

    private JBPanel<?> crateStepContent() {
        var mainPanel = new JBPanel<>();
        JBLabel text = new JBLabel("Identify the target application");
        text.setFont(text.getFont().deriveFont(Font.BOLD, 18f));

        JBLabel subText = new JBLabel(
                """
                         Tell us which application you want to connect to. Based on this information,\s
                         the system will identify the target and locate appropriate documentation.
                         """
        );
        subText.setBorder(JBUI.Borders.emptyBottom(15));

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(text);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subText);
        mainPanel.add(createBasicSettingForm());

        return mainPanel;
    }

    private JBPanel<?> createBasicSettingForm() {
        applicationNameField = new JBTextField();
        versionField = new JBTextField();
        descriptionArea = new JBTextArea(4, 40);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        integrationTypeCombo = new ComboBox<>(Stream.concat(
                Stream.of(COMBO_BOX_ITEM_UNDEFINED),
                Arrays.stream(ConnDevIntegrationType.values()).map(Enum::name)
        ).toArray(String[]::new));
        deploymentTypeCombo = new ComboBox<>(Stream.concat(
                Stream.of(COMBO_BOX_ITEM_UNDEFINED),
                Arrays.stream(ConnDevDeploymentType.values()).map(Enum::name)
        ).toArray(String[]::new));

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

        var form = new JBPanel<>(new BorderLayout());
        form.setMinimumSize(new Dimension((int) (form.getWidth() * 0.7), (int) (form.getHeight() * 0.7)));
        form.add(FormBuilder.createFormBuilder()
                .addLabeledComponent("Application name *", applicationNameField)
                .addLabeledComponent("Description", new JBScrollPane(descriptionArea))
                .addLabeledComponent("Version of application", versionField)
                .addLabeledComponent("Integration type *", integrationTypeCombo)
                .addLabeledComponent("Deployment type", deploymentTypeCombo)
                .getPanel());

        return form;
    }
}
