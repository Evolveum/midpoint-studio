package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DiscoverDocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.*;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class CredentialsPanel extends JBPanel<DiscoverDocumentationPanel> implements WizardContent {

    public CredentialsPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 15, true, false));
        setBorder(JBUI.Borders.empty(20));

        add(new JBLabel("Authentication Method for Testing") {{
            setFont(JBUI.Fonts.label(18f));
        }});

        add(new JBLabel("<html>Choose an authentication method for this connector and fill in the corresponding " +
                "credential fields for testing. The fields below will update based on the selected method.</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});

        JBPanel<?> container = new JBPanel<>(new GridBagLayout());
        container.setBackground(UIUtil.getTextFieldBackground());
        container.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        container.add(new JBLabel("Authentication method") {{
            setBorder(JBUI.Borders.empty(15, 15, 10, 15));
            setForeground(UIUtil.getContextHelpForeground());
        }}, gbc);

        ButtonGroup authGroup = new ButtonGroup();

        JBRadioButton jwtRadio = new JBRadioButton("OIDC provider generated JWT as a Bearer token");
        authGroup.add(jwtRadio);
        gbc.gridy++;
        container.add(createRadioRow(jwtRadio), gbc);

        JBRadioButton apiKeyRadio = new JBRadioButton("API Key through Basic Auth", true);
        authGroup.add(apiKeyRadio);
        gbc.gridy++;
        container.add(createRadioRow(apiKeyRadio), gbc);

        gbc.gridy++;
        container.add(createNestedForm(), gbc);

        add(container);
    }

    private JPanel createRadioRow(JBRadioButton radio) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1, 0, 0, 0));
        radio.setBorder(JBUI.Borders.empty(12, 15));
        row.add(radio, BorderLayout.CENTER);
        return row;
    }

    private JPanel createNestedForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(JBUI.Borders.empty(0, 45, 20, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JPanel userLabelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        userLabelRow.setOpaque(false);
        userLabelRow.add(new JBLabel("REST Username *") {{ setFont(getFont().deriveFont(Font.BOLD)); }});
        userLabelRow.add(new JBLabel(AllIcons.General.ContextHelp));
        form.add(userLabelRow, gbc);

        gbc.gridy = 1; gbc.insets = JBUI.insets(5, 0, 15, 0);
        form.add(new JBTextField("apikey"), gbc);

        gbc.gridy = 2; gbc.insets = JBUI.emptyInsets();
        form.add(new JBLabel("REST Password") {{ setFont(getFont().deriveFont(Font.BOLD)); }}, gbc);

        gbc.gridy = 3;
        form.add(new JBRadioButton("Use clear value", true), gbc);
        gbc.gridy = 4;
        form.add(new JBRadioButton("Use secret provider"), gbc);

        gbc.gridy = 5; gbc.insets = JBUI.insetsTop(10);
        JBPasswordField pwdField = new JBPasswordField();
        pwdField.setText("password123");
        form.add(pwdField, gbc);

        return form;
    }

    @Override
    public void beforeChangeAction() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
