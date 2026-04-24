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

public class BaseUrlSpecificationPanel extends JBPanel<DiscoverDocumentationPanel> implements WizardContent {

    public BaseUrlSpecificationPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 20, 15, true, false));
        setBorder(JBUI.Borders.empty(20));

        add(new JBLabel("Set Base API URL") {{
            setFont(JBUI.Fonts.label(18f).deriveFont(Font.PLAIN));
        }});

        add(new JBLabel("<html>Enter the root URL of the target system's API or select one from suggested if they are available. " +
                "This address will be used as the starting point for all communication...</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});

        add(createInfoBanner());

        JBPanel<?> formPanel = new JBPanel<>(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        labelRow.setOpaque(false);
        labelRow.add(new JBLabel("REST Base Address *") {{
            setFont(getFont().deriveFont(Font.BOLD));
        }});
        labelRow.add(new JBLabel(AllIcons.General.ContextHelp));

        formPanel.add(labelRow, gbc);

        gbc.gridy = 1;
        gbc.insets = JBUI.insetsTop(5);
        JBTextField urlField = new JBTextField("ht");
        urlField.putClientProperty("JTextField.Search.cancelAction", (Runnable) () -> urlField.setText(""));
        urlField.putClientProperty("JTextField.variant", "search");

        formPanel.add(urlField, gbc);

        add(formPanel);
    }

    private JPanel createInfoBanner() {
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        banner.setBackground(new Color(235, 225, 255));
        banner.setBorder(JBUI.Borders.customLine(new Color(180, 150, 240), 1));

        JBLabel icon = new JBLabel(AllIcons.General.ProjectConfigurable);
        icon.setForeground(new Color(110, 40, 220));

        JBLabel text = new JBLabel("<html><b>Endpoints identified</b> A likely URLs for the test purposes from your documentation were detected</html>");
        text.setForeground(new Color(110, 40, 220));

        banner.add(icon);
        banner.add(text);
        return banner;
    }

    @Override
    public void beforeChangeAction() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
