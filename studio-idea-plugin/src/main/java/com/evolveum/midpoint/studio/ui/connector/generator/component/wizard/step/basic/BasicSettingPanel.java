package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class BasicSettingPanel extends JBPanel<BasicSettingPanel> implements WizardContent {

    private final ConnectorGeneratorDialogContext dialogContext;
    private Runnable updateDialogContext;

    public BasicSettingPanel(ConnectorGeneratorDialogContext dialogContext) {
        this.dialogContext = dialogContext;
        setLayout(new BorderLayout());
        add(applicationIdentificationPanel());
    }

    private JPanel applicationIdentificationPanel() {
        JLabel header = new JLabel("Identify the Target Application");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));

        JLabel description = new JLabel(
                "Tell us which application you want to connect to. Based on this information, the system will identify the target and locate appropriate documentation."
        );
        description.setBorder(JBUI.Borders.emptyBottom(15));

        var applicationNameField = new JBTextField();
        var descriptionArea = new JBTextArea(4, 40);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        var versionField = new JBTextField();

        var integrationTypeCombo = new ComboBox<>(new String[]{
                "Undefined", "REST", "SOAP", "Database", "Custom"
        });

        var deploymentTypeCombo = new ComboBox<>(new String[]{
                "Undefined", "Cloud", "On-Premise", "Hybrid"
        });

        JScrollPane descriptionScroll = new JBScrollPane(descriptionArea);

        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Application name *", applicationNameField)
                .addLabeledComponent("Description", descriptionScroll)
                .addLabeledComponent("Version of application", versionField)
                .addLabeledComponent("Integration type *", integrationTypeCombo)
                .addLabeledComponent("Deployment type", deploymentTypeCombo)
                .getPanel();

        JPanel mainPanel = new JBPanel<>(new BorderLayout(0, 10));
        mainPanel.setBorder(JBUI.Borders.empty(15));

        JPanel topPanel = new JBPanel<>();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(header);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(description);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        mainPanel.setPreferredSize(new Dimension(600, 400));

        updateDialogContext = () -> {
            ConnDevApplicationInfoType connDevApplicationInfoType = dialogContext.getConnDevApplicationInfoType();
            connDevApplicationInfoType.setApplicationName(PolyStringType.fromOrig(applicationNameField.getText()));
            connDevApplicationInfoType.setDescription(PolyStringType.fromOrig(descriptionArea.getText()));
            connDevApplicationInfoType.setVersion(versionField.getText());
            connDevApplicationInfoType.setIntegrationType(null);
            connDevApplicationInfoType.setDeploymentType(null);
        };

        return mainPanel;
    }

    @Override
    public void onStateChanged() {
        this.updateDialogContext.run();

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
