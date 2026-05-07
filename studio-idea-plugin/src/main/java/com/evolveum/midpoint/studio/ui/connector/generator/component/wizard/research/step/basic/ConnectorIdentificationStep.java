package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.ConnectorGeneratorWizardData;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class ConnectorIdentificationStep extends StepAdapter {

    private final MidPointClient client;
    private final ConnectorGeneratorWizardData dataModel;
    private final JBPanel<?> panel = new JBPanel<>();

    public ConnectorIdentificationStep(MidPointClient client, ConnectorGeneratorWizardData dataModel) {
        this.client = client;
        this.dataModel = dataModel;
        panel.add(createTopBanner());
        panel.add(createConnectorIdentificationForm());
        panel.setName("Application Identification");
    }

    private JBPanel<?> createTopBanner() {
        JBPanel<?> topPanel = new JBPanel<>();

        JBLabel description = new JBLabel("""
                On this panel you can enter the fundamental details that describe the connector,\s
                providing the necessary context before adding more specific configuration.
               """
        );
        description.setBorder(JBUI.Borders.emptyBottom(15));

        JBLabel header = new JBLabel("Identify the target application");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(new JBLabel("Set basic information about the connector"));
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
    public void _commit(boolean b) throws CommitStepException {
        // TODO impl processing data model
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }
}
