package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class ConnectorIdentificationPanel extends JBPanel<ConnectorIdentificationPanel> implements WizardContent {

    public ConnectorIdentificationPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 20, 10, true, false));
        setBorder(JBUI.Borders.empty(20));

        add(new JBLabel("Set Basic Information About the Connector", SwingConstants.LEFT) {{
            setFont(getFont().deriveFont(Font.BOLD, 16f));
        }});

        add(new JBLabel("<html>On this panel you can enter the fundamental details that describe the connector, " +
                "providing the necessary context before adding more specific configuration.</html>") {{
            setForeground(UIManager.getColor("Label.disabledForeground"));
        }});

        JBPanel<?> formPanel = new JBPanel<>(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = JBUI.insets(10, 0, 5, 0);

        formPanel.add(createLabel("Group id *"), gbc);
        gbc.gridy++;
        formPanel.add(new JBTextField("com.evolveum.polygon.community"), gbc);

        gbc.gridy++;
        formPanel.add(createLabel("Artifact id *"), gbc);
        gbc.gridy++;
        formPanel.add(new JBTextField("openproject"), gbc);

        gbc.gridy++;
        formPanel.add(createLabel("Display name"), gbc);
        gbc.gridy++;
        formPanel.add(new JBTextField(), gbc);

        gbc.gridy++;
        formPanel.add(createLabel("Description"), gbc);
        gbc.gridy++;
        formPanel.add(new JTextArea(3, 20) {{
            setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
        }}, gbc);

        gbc.gridy++;
        formPanel.add(createLabel("Version *"), gbc);
        gbc.gridy++;
        formPanel.add(new JBTextField("1.0"), gbc);

        add(formPanel);
    }

    private JBLabel createLabel(String text) {
        JBLabel label = new JBLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
