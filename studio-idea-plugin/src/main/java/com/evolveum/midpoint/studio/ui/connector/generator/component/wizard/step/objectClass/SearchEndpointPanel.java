package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.objectClass;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class SearchEndpointPanel extends JBPanel<DocumentationPanel> implements WizardContent {

    public SearchEndpointPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 15, true, false));
        setBorder(JBUI.Borders.empty(20));

        add(new JBLabel("Select and Adjust Search Operation Endpoints") {{
            setFont(JBUI.Fonts.label(18f));
        }});

        add(new JBLabel("<html>Select endpoint to be used in search operation. The assistant may suggest default search endpoints or parameters " +
                "based on available documentation. If no suggestion fits, you can specify a custom search configuration manually.</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});

        add(createEndpointRow("GET", "List users", "/api/v3/users", true));
    }

    private JPanel createEndpointRow(String method, String label, String path, boolean selected) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(UIUtil.getTextFieldBackground());
        row.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = JBUI.insets(10, 15);
        gbc.gridx = 0;
        row.add(new JBRadioButton("", selected), gbc);

        gbc.gridx = 1;
        gbc.insets = JBUI.insetsRight(15);
        JBLabel methodLabel = new JBLabel(method);
        methodLabel.setForeground(UIUtil.getInactiveTextColor());
        methodLabel.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 0, 0, 0, 1));
        methodLabel.setPreferredSize(new Dimension(40, 20));
        row.add(methodLabel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        row.add(new JBLabel(label) {{ setForeground(UIUtil.getContextHelpForeground()); }}, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        row.add(new JBLabel(path) {{ setForeground(UIUtil.getInactiveTextColor()); }}, gbc);

        return row;
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
