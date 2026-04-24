package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DiscoverDocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class TestConnectionPanel extends JBPanel<DiscoverDocumentationPanel> implements WizardContent {

    public TestConnectionPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(20));

        JBPanel<?> contentPanel = new JBPanel<>(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 15, true, false));

        contentPanel.add(new JBLabel("Provide Endpoint for Connection Test") {{
            setFont(JBUI.Fonts.label(18f));
        }});

        contentPanel.add(new JBLabel("<html>Determine the best endpoint for running a test operation. This helps validate " +
                "that the connector can communicate with the target system and start discovering available data structures.</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});

        JBPanel<?> formPanel = new JBPanel<>(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        labelRow.setOpaque(false);
        labelRow.add(new JBLabel("Test Endpoint *") {{
            setFont(getFont().deriveFont(Font.BOLD));
        }});
        labelRow.add(new JBLabel(AllIcons.General.ContextHelp));
        formPanel.add(labelRow, gbc);

        gbc.gridy = 1;
        gbc.insets = JBUI.insetsTop(8);
        JBTextField endpointField = new JBTextField("/api/v3/my_preferences");

        endpointField.putClientProperty("JTextField.variant", "search");
        endpointField.putClientProperty("JTextField.Search.cancelAction", (Runnable) () -> endpointField.setText(""));

        formPanel.add(endpointField, gbc);
        contentPanel.add(formPanel);

        add(contentPanel, BorderLayout.CENTER);

        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBorder(JBUI.Borders.emptyTop(20));
        actionBar.setOpaque(false);

        JButton backButton = new JButton("Back", AllIcons.Actions.Back);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setForeground(new Color(70, 130, 180)); // Muted blue
        actionBar.add(backButton, BorderLayout.WEST);

        JButton testButton = new JButton("Test connection", AllIcons.General.ContextHelp);
        actionBar.add(testButton, BorderLayout.EAST);

        add(actionBar, BorderLayout.SOUTH);
    }

    @Override
    public void beforeChangeAction() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
