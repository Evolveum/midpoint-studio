package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.ConnectorGeneratorWizardData;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class ApplicationIdentificationStep extends StepAdapter {

    private final MidPointClient client;
    private final ConnectorGeneratorWizardData dataModel;
    private final JBPanel<?> panel = new JBPanel<>();

    private JBTextField applicationNameField;
    private JBTextField versionField;
    private JBTextArea descriptionArea;
    private ComboBox<String> integrationTypeCombo;
    private ComboBox<String> deploymentTypeCombo;

    private final String COMBO_BOX_ITEM_UNDEFINED = "Undefined";

    public ApplicationIdentificationStep(MidPointClient client, ConnectorGeneratorWizardData dataModel) {
        this.client = client;
        this.dataModel = dataModel;
        panel.add(createTopBanner());
        panel.add(createBasicSettingForm());
        panel.setName("Application Identification");
    }

    private JBPanel<?> createTopBanner() {
        JBPanel<?> topPanel = new JBPanel<>();

        JBLabel description = new JBLabel(
                """
                        Tell us which application you want to connect to. Based on this information,\s
                        the system will identify the target and locate appropriate documentation.
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

        var form = new JBPanel<>(new BorderLayout());
        form.add(FormBuilder.createFormBuilder()
                .addLabeledComponent("Application name *", applicationNameField)
                .addLabeledComponent("Description", new JBScrollPane(descriptionArea))
                .addLabeledComponent("Version of application", versionField)
                .addLabeledComponent("Integration type *", integrationTypeCombo)
                .addLabeledComponent("Deployment type", deploymentTypeCombo)
                .getPanel());

        return form;
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {

        if (applicationNameField.getText() == null && applicationNameField.getText().isBlank()) {
            throw new CommitStepException("Application Name is required field");
        }

        if (integrationTypeCombo.getSelectedItem() == null &&
                integrationTypeCombo.getSelectedItem().equals(COMBO_BOX_ITEM_UNDEFINED) ) {
            throw new CommitStepException("Integration Type is required field");
        }

        ProgressManager.getInstance().runProcessWithProgressSynchronously(
                () -> {
                    try {
                        var connectorDevelopmentType = new ConnectorDevelopmentType();
                        connectorDevelopmentType.setApplication(getConnDevApplicationInfoType());
                        connectorDevelopmentType.setName(connectorDevelopmentType.getApplication().getApplicationName());
                        dataModel.connectorDevelopmentType = client.createConnectorDevelopmentType(connectorDevelopmentType);
                    } catch (Exception e) {
                        try {
                            throw new CommitStepException(e.getMessage());
                        } catch (CommitStepException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                },
                "Creating ConnectorDevelopmentType",
                true,
                client.getProject()
        );
    }

    private @NotNull ConnDevApplicationInfoType getConnDevApplicationInfoType() {
        var connDevApplicationInfoType = new ConnDevApplicationInfoType();
        connDevApplicationInfoType.setApplicationName(new PolyStringType(applicationNameField.getText()));
        connDevApplicationInfoType.version(versionField.getText());
        connDevApplicationInfoType.description(descriptionArea.getText());
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
}
