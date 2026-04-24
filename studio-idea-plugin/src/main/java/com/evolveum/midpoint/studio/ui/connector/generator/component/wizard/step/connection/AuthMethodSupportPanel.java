package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DiscoverDocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class AuthMethodSupportPanel extends JBPanel<DiscoverDocumentationPanel> implements WizardContent {

    public AuthMethodSupportPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(JBUI.Borders.empty(20));

        add(new JBLabel("Select Supported Authentication Methods") {{
            setFont(JBUI.Fonts.label(18f));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }});

        add(Box.createVerticalStrut(10));

        add(new JBLabel("<html>Choose which authentication methods your connector should support. Users will be able to authenticate...</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }});

        add(Box.createVerticalStrut(20));

        JBPanel<?> listContainer = new JBPanel<>(new GridLayout(0, 1));
        listContainer.setBackground(UIUtil.getPanelBackground());
        listContainer.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1));

        listContainer.add(createAuthRow("OIDC provider generated JWT as a Bearer token", "Bearer", "Authorization using Bearer token."));
        listContainer.add(createAuthRow("API Key through Basic Auth", "Basic", "Basic authorization using username and password"));
        listContainer.add(createAuthRow("BasicAuth", "Basic", "Basic authorization using username and password"));

        add(listContainer);
    }

    private JPanel createAuthRow(String title, String badgeText, String description) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(UIUtil.getTextFieldBackground());
        row.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 0, 0, 1, 0));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = JBUI.insets(15);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 2;
        row.add(new JBCheckBox(), gbc);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(new JBLabel(title) {{ setFont(getFont().deriveFont(Font.BOLD)); }});
        titlePanel.add(createBadge(badgeText));

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(15, 0, 2, 15);
        row.add(titlePanel, gbc);

        gbc.gridy = 1;
        gbc.insets = JBUI.insets(0, 0, 15, 15);
        JBLabel descLabel = new JBLabel(description);
        descLabel.setForeground(UIUtil.getContextHelpForeground());
        descLabel.setFont(JBUI.Fonts.smallFont());
        row.add(descLabel, gbc);

        return row;
    }

    private JPanel createBadge(String text) {
        JBLabel label = new JBLabel(text.toUpperCase());
        label.setForeground(Color.WHITE);
        label.setFont(JBUI.Fonts.miniFont());

        JPanel badge = new JPanel(new BorderLayout());
        badge.setBackground(new Color(0, 128, 128));
        badge.setBorder(JBUI.Borders.empty(2, 6));
        badge.add(label);
        return badge;
    }

    @Override
    public void beforeChangeAction() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
