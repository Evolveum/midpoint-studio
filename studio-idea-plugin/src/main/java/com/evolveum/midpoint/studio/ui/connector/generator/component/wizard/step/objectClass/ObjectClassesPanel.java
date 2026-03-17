package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.objectClass;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class ObjectClassesPanel extends JBPanel<DocumentationPanel> implements WizardContent {

    public ObjectClassesPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 15, true, false));
        setBorder(JBUI.Borders.empty(20));

        add(new JBLabel("Select Object Class to Be Configured") {{
            setFont(JBUI.Fonts.label(18f));
        }});

        add(new JBLabel("<html>The midPilot analyzed your documentation and found object classes that might be available for integration. " +
                "Select one to begin its configuration.</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});

        add(createTealBanner());

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        sectionHeader.add(new JBLabel("Recommended Object Classes") {{
            setFont(getFont().deriveFont(Font.BOLD));
            setForeground(UIUtil.getContextHelpForeground());
        }}, BorderLayout.WEST);

        JBLabel moreLink = new JBLabel("More classes", AllIcons.General.Settings, SwingConstants.RIGHT);
        moreLink.setForeground(UIUtil.getInactiveTextColor());
        sectionHeader.add(moreLink, BorderLayout.EAST);
        add(sectionHeader);

        JBPanel<?> gridPanel = new JBPanel<>(new GridLayout(0, 3, 10, 10));
        gridPanel.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        gridPanel.add(createObjectCard("User", "Principal representing an individual account holder", true, group));
        gridPanel.add(createObjectCard("Principal", "", false, group));
        gridPanel.add(createObjectCard("Group", "Principal representing a collection of users", false, group));
        gridPanel.add(createObjectCard("Role", "Set of permissions granted to a principal within a project", false, group));

        add(gridPanel);
    }

    private JPanel createTealBanner() {
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        banner.setBackground(new Color(45, 150, 165));

        JBLabel text = new JBLabel("<html><b>Not seeing what you expected?</b> Try checking the list of other object classes using the 'More classes' button.</html>");
        text.setForeground(Color.WHITE);
        text.setIcon(AllIcons.General.Information);

        banner.add(text);
        return banner;
    }

    private JPanel createObjectCard(String title, String description, boolean selected, ButtonGroup group) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtil.getTextFieldBackground());
        card.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1));
        GridBagConstraints gbc = new GridBagConstraints();

        JBRadioButton radio = new JBRadioButton("", selected);
        group.add(radio);

        gbc.insets = JBUI.insets(10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 2;
        card.add(radio, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(10, 0, 0, 10);
        card.add(new JBLabel(title) {{ setFont(getFont().deriveFont(Font.BOLD)); }}, gbc);

        gbc.gridy = 1;
        gbc.insets = JBUI.insets(0, 0, 10, 10);
        JBLabel descLabel = new JBLabel("<html>" + description + "</html>");
        descLabel.setFont(JBUI.Fonts.smallFont());
        descLabel.setForeground(UIUtil.getContextHelpForeground());
        card.add(descLabel, gbc);

        return card;
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
