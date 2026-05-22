package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class ApplicationIdentificationPanel extends JBPanel<ApplicationIdentificationPanel> implements WizardContent, Disposable {

    private final ConnectorGeneratorDataModel dataModel;
    private Runnable updateDatModelByFrom;

    private final String COMBO_BOX_ITEM_UNDEFINED = "Undefined";

    private boolean validForm = false;

    public ApplicationIdentificationPanel(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
        setLayout(new BorderLayout());
        createApplicationIdentificationPanel();
    }

    private void createApplicationIdentificationPanel() {
        add(createTopBanner(), BorderLayout.NORTH);
        add(createBasicSettingForm(), BorderLayout.CENTER);
    }

    private JBPanel<?> createTopBanner() {
        JBPanel<?> topPanel = new JBPanel<>();

        JBLabel description = new JBLabel(
                """
                        Tell us which application you want to connect to. Based on this information, 
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
        var applicationNameField = new JBTextField();
        var versionField = new JBTextField();
        var descriptionArea = new JBTextArea(4, 40);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        ComboBox<String> integrationTypeCombo = new ComboBox<>(Stream.concat(
                Stream.of(COMBO_BOX_ITEM_UNDEFINED),
                Arrays.stream(ConnDevIntegrationType.values()).map(Enum::name)
        ).toArray(String[]::new));

        ComboBox<String> deploymentTypeCombo = new ComboBox<>(Stream.concat(
                Stream.of(COMBO_BOX_ITEM_UNDEFINED),
                Arrays.stream(ConnDevDeploymentType.values()).map(Enum::name)
        ).toArray(String[]::new));

        if (dataModel.getConnectorDevelopmentType() != null) {
            var connDevApplicationInfoType = dataModel.getConnectorDevelopmentType().getApplication();
            applicationNameField.setText(connDevApplicationInfoType.getApplicationName().getNorm());
//            descriptionArea.setText(connDevApplicationInfoType.getDescription().getNorm());
            versionField.setText(connDevApplicationInfoType.getVersion());
            integrationTypeCombo.setItem(connDevApplicationInfoType.getIntegrationType().value());
            deploymentTypeCombo.setItem(connDevApplicationInfoType.getDeploymentType().value());
        }

        updateDatModelByFrom = () -> {
            var applicationNameFieldValidator = new ComponentValidator(this)
                    .withValidator(() -> {
                        var value = applicationNameField.getText();
                        return (value == null || value.isBlank())
                                ? new ValidationInfo("Application Name is required field", applicationNameField).withOKEnabled()
                                : null;
                    }).installOn(applicationNameField);
            applicationNameFieldValidator.revalidate();
            applicationNameField.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    ComponentValidator.getInstance(applicationNameField).ifPresent(ComponentValidator::revalidate);
                }
            });

            var integrationTypeComboValidator = new ComponentValidator(this)
                    .withValidator(() -> {
                        var value = integrationTypeCombo.getSelectedItem();
                        return (value == null || value.equals(COMBO_BOX_ITEM_UNDEFINED))
                                ? new ValidationInfo("Integration Type is required field", integrationTypeCombo).withOKEnabled()
                                : null;
                    }).installOn(integrationTypeCombo);
            integrationTypeComboValidator.revalidate();
            integrationTypeCombo.addItemListener(e -> {
                ComponentValidator.getInstance(integrationTypeCombo).ifPresent(ComponentValidator::revalidate);
            });

            this.validForm = applicationNameFieldValidator.getValidationInfo() == null &&
                    integrationTypeComboValidator.getValidationInfo() == null;

            var connDevApplicationInfoType = new ConnDevApplicationInfoType();
            connDevApplicationInfoType.setApplicationName(
                    new PolyStringType(applicationNameField.getText()));
//            connDevApplicationInfoType.setDescription(
//                    new PolyStringType(descriptionArea.getText()));
            connDevApplicationInfoType.setVersion(
                    versionField.getText());
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

            var connectorDevelopmentType = new ConnectorDevelopmentType();
            connectorDevelopmentType.setName(
                    new PolyStringType(connDevApplicationInfoType.getApplicationName().getOrig()));
            connectorDevelopmentType.setApplication(connDevApplicationInfoType);
            dataModel.setConnectorDevelopmentType(connectorDevelopmentType);
        };

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
    public void beforeChangeAction() {
        this.updateDatModelByFrom.run();

        if (validForm) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                    () -> {
                        try {
                            EnvironmentService em = EnvironmentService.getInstance(dataModel.getProject());
                            Environment env = em.getSelected();
                            var client = new MidPointClient(dataModel.getProject(), env);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "Creating ConnectorDevelopmentType",
                    true,
                    dataModel.getProject()
            );
        }
    }

    @Override
    public boolean disableChangeStep() {
        return !validForm;
    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }

    @Override
    public void dispose() {

    }
}
