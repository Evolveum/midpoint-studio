package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class ApplicationIdentificationPanel extends JBPanel<ApplicationIdentificationPanel> implements WizardContent {

    private final ConnectorGeneratorDialogContext dialogContext;
    private Runnable updateDialogContextByFrom;

    private final String COMBO_BOX_ITEM_UNDEFINED = "Undefined";

    public ApplicationIdentificationPanel(ConnectorGeneratorDialogContext dialogContext) {
        this.dialogContext = dialogContext;
        setLayout(new BorderLayout());
        createApplicationIdentificationPanel();
    }

    private void createApplicationIdentificationPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(JBUI.Borders.empty(15));
        add(createTopBanner(), BorderLayout.NORTH);
        add(createBasicSettingForm(), BorderLayout.CENTER);
    }

    private JPanel createTopBanner() {
        JPanel topPanel = new JBPanel<>();

        JLabel description = new JLabel(
                """
                        Tell us which application you want to connect to. Based on this information, 
                        the system will identify the target and locate appropriate documentation.
                        """
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

    private JPanel createBasicSettingForm() {
        var applicationNameField = new JBTextField();
        var descriptionArea = new JBTextArea(4, 40);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        var versionField = new JBTextField();

        ComboBox<String> integrationTypeCombo = new ComboBox<>(Stream.concat(
                Stream.of(COMBO_BOX_ITEM_UNDEFINED),
                Arrays.stream(ConnDevIntegrationType.values()).map(Enum::name)
        ).toArray(String[]::new));

        ComboBox<String> deploymentTypeCombo = new ComboBox<>(Stream.concat(
                Stream.of(COMBO_BOX_ITEM_UNDEFINED),
                Arrays.stream(ConnDevDeploymentType.values()).map(Enum::name)
        ).toArray(String[]::new));

        JScrollPane descriptionScroll = new JBScrollPane(descriptionArea);


        if (dialogContext.getConnectorDevelopmentType() != null) {
            var connDevApplicationInfoType = dialogContext.getConnectorDevelopmentType().getApplication();
            applicationNameField.setText(connDevApplicationInfoType.getApplicationName().getNorm());
            descriptionArea.setText(connDevApplicationInfoType.getDescription().getNorm());
            versionField.setText(connDevApplicationInfoType.getVersion());
            integrationTypeCombo.setItem(connDevApplicationInfoType.getIntegrationType().value());
            deploymentTypeCombo.setItem(connDevApplicationInfoType.getDeploymentType().value());
        }

        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Application name *", applicationNameField)
                .addLabeledComponent("Description", descriptionScroll)
                .addLabeledComponent("Version of application", versionField)
                .addLabeledComponent("Integration type *", integrationTypeCombo)
                .addLabeledComponent("Deployment type", deploymentTypeCombo)
                .getPanel();

        updateDialogContextByFrom = () -> {
            var connDevApplicationInfoType = new ConnDevApplicationInfoType();

            connDevApplicationInfoType.setApplicationName(
                    new PolyStringType(applicationNameField.getText()));
            connDevApplicationInfoType.setDescription(
                    new PolyStringType(descriptionArea.getText()));
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
            dialogContext.setConnectorDevelopmentType(connectorDevelopmentType);
        };

        return formPanel;
    }

    @Override
    public void beforeChangeAction() throws InterruptedException {
        // FIXME implementation validate formular values required / unrepaired
        this.updateDialogContextByFrom.run();

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
                "Creating ConnectorDevelopmentType",
                true,
                dialogContext.getProject()
        );
    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
