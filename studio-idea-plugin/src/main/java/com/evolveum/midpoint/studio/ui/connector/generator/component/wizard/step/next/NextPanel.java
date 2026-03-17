package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.next;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NextPanel extends JBPanel<DocumentationPanel> implements WizardContent {

    public NextPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 10, true, false));
        setBorder(JBUI.Borders.empty(30));
        setBackground(new Color(242, 243, 246));

        add(new JBLabel("What to do next?") {{
            setFont(JBUI.Fonts.label(20f));
        }});

        add(new JBLabel("<html>You've successfully configured the basic search functionality for the User object type. " +
                "Based on your progress, here are some recommended next steps...</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});

        add(createSectionTitle("User"));
        JBPanel<?> userGrid = new JBPanel<>(new GridLayout(0, 4, 12, 12));
        userGrid.setOpaque(false);
        userGrid.add(createActionCard("Configure schema", "Define structure for object class...", AllIcons.Nodes.Class));
        userGrid.add(createActionCard("Configure search operation", "Define how objects are queried...", AllIcons.Actions.Search));
        userGrid.add(createActionCard("Configure create operation", "Define how new objects are created...", AllIcons.General.Add));
        userGrid.add(createActionCard("Configure modify operation", "Specify how existing objects are updated...", AllIcons.Actions.Edit));
        userGrid.add(createActionCard("Configure delete operation", "Define how objects are removed...", AllIcons.Actions.GC));
        add(userGrid);

        add(createSectionTitle("Connector actions"));
        JBPanel<?> connectorGrid = new JBPanel<>(new GridLayout(0, 4, 12, 12));
        connectorGrid.setOpaque(false);
        connectorGrid.add(createActionCard("Create resource", "Creates a new resource for operations.", AllIcons.General.Add));
        connectorGrid.add(createActionCard("Upload connector", "Upload the prepared connector JAR.", AllIcons.Nodes.Deploy));

        JPanel highlightedCard = createActionCard("Add new object class", "Start configuration for a different class...", AllIcons.Nodes.AbstractClass);
        highlightedCard.setBorder(JBUI.Borders.customLine(new Color(70, 130, 180), 2));
        connectorGrid.add(highlightedCard);

        add(connectorGrid);
    }

    private JBLabel createSectionTitle(String text) {
        return new JBLabel(text) {{
            setFont(getFont().deriveFont(Font.BOLD, 14f));
            setBorder(JBUI.Borders.empty(20, 0, 5, 0));
        }};
    }

    private JPanel createActionCard(String title, String description, Icon icon) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtil.getTextFieldBackground());
        card.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 2;
        gbc.insets = JBUI.insets(15);
        card.add(new JBLabel(icon), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1; gbc.weightx = 1.0;
        gbc.insets = JBUI.insets(15, 0, 2, 15);
        card.add(new JBLabel(title) {{ setFont(getFont().deriveFont(Font.BOLD)); }}, gbc);

        gbc.gridy = 1;
        gbc.insets = JBUI.insets(0, 0, 15, 15);
        JBLabel descLabel = new JBLabel("<html>" + description + "</html>");
        descLabel.setFont(JBUI.Fonts.smallFont());
        descLabel.setForeground(UIUtil.getContextHelpForeground());
        card.add(descLabel, gbc);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(JBUI.Borders.customLine(new Color(70, 130, 180), 1));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1));
            }
        });

        return card;
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return null;
    }
}
