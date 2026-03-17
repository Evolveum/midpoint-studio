package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class DocumentationPanel extends JBPanel<DocumentationPanel> implements WizardContent {

    private final ConnectorGeneratorDialogContext dialogContext;

    public DocumentationPanel(ConnectorGeneratorDialogContext dialogContext) {
        this.dialogContext = dialogContext;
        add(documentationPanel());
    }

    private JPanel documentationPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Provide Integration Documentation");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel descriptionLabel = new JLabel("<html><body><p style='color: #666;'>Documentation helps the AI understand how your system communicates, which endpoints it exposes, and what data structures it uses. By analyzing this information, the AI can generate a more accurate and tailored connector for your integration.</p></body></html>");
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(descriptionLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel infoMessagePanel = new JPanel(new BorderLayout(10, 0));
        infoMessagePanel.setBackground(new Color(243, 229, 245));
        infoMessagePanel.setOpaque(true);
        infoMessagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 190, 231)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        infoMessagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoIcon = new JLabel("");
        infoIcon.setForeground(new Color(171, 71, 188));
        infoIcon.setFont(new Font(Font.SERIF, Font.PLAIN, 18));
        infoMessagePanel.add(infoIcon, BorderLayout.WEST);

        JLabel infoText = new JLabel("<html><body><b>Documentation found</b> AI has found matching documentation or configurations. Please review them to ensure they fit your needs.</body></html>");
        infoText.setForeground(new Color(123, 31, 162));
        infoMessagePanel.add(infoText, BorderLayout.CENTER);

        mainPanel.add(infoMessagePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel selectAllPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        selectAllPanel.add(new JCheckBox());
        JComboBox<String> multiActionCombo = new JComboBox<>(new String[]{" "});
        multiActionCombo.setPreferredSize(new Dimension(30, multiActionCombo.getPreferredSize().height));
        selectAllPanel.add(multiActionCombo);
        toolbarPanel.add(selectAllPanel, BorderLayout.WEST);

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton addUrlButton = new JButton("+ Add URL");
        JButton uploadFileButton = new JButton("↑ Upload file");
        actionButtonsPanel.add(addUrlButton);
        actionButtonsPanel.add(uploadFileButton);
        toolbarPanel.add(actionButtonsPanel, BorderLayout.EAST);

        mainPanel.add(toolbarPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel itemListPanel = new JPanel();
        itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));
        itemListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemListPanel.setOpaque(false);

        itemListPanel.add(createDocumentationItem("API v3 usage example",
                "Open source project management software for classic, agile or hybrid project management: task management✓ Gantt charts✓ boards✓ team collaboration✓ time and cost reporting✓ FREE trial!",
                "https://www.openproject.org/docs/api/example/"));

        JPanel selectedItem = createDocumentationItem("OpenProject API Introduction",
                "<html><body><strong>The documentation for the APIv3 is written according to the OpenAPI 3.1 Specification</strong>. You can either view the static version of this documentation on the website or the interactive version, rendered with OpenAPI Explorer, in your OpenProject installation under /api/docs.</body></html>",
                "https://www.openproject.org/docs/api/introduction/");
        selectedItem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 181, 246)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        itemListPanel.add(selectedItem);

        itemListPanel.add(createDocumentationItem("API documentation",
                "<html><body>Please note that we intend to keep this specification as accurate and stable as possible, however work on the <strong>API</strong> is still ongoing and not all resources and actions in <strong>OpenProject</strong> are yet accessible through the <strong>API</strong>. <strong>This</strong> <strong>document</strong> will be subject to changes as we add more endpoints and functionality ...</body></html>",
                "https://www.openproject.org/docs/api/"));

        itemListPanel.add(createDocumentationItem("OpenProject OpenAPI specification",
                "OpenAPI specification",
                "https://www.openproject.org/docs/api/v3/spec.yml"));

        mainPanel.add(itemListPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(scrollPane, BorderLayout.CENTER);

        return outerPanel;
    }

    private JPanel createDocumentationItem(String title, String description, String url) {
        JPanel itemPanel = new JPanel(new GridBagLayout());
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15));
        itemPanel.setBorder(defaultBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        itemPanel.add(new JCheckBox(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 10, 0, 0);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titlePanel.add(titleLabel);

        JLabel aiBadge = new JLabel(" % AI ");
        aiBadge.setOpaque(true);
        aiBadge.setBackground(new Color(230, 220, 240)); // Light badge background
        aiBadge.setForeground(new Color(150, 50, 170)); // AI text color
        aiBadge.setFont(aiBadge.getFont().deriveFont(Font.BOLD, 10));
        aiBadge.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        titlePanel.add(aiBadge);

        itemPanel.add(titlePanel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel actionIconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionIconsPanel.setOpaque(false);
        JLabel viewIcon = new JLabel("");
        viewIcon.setToolTipText("View documentation");
        viewIcon.setForeground(Color.GRAY);
        actionIconsPanel.add(viewIcon);

        JLabel deleteIcon = new JLabel("");
        deleteIcon.setToolTipText("Delete documentation entry");
        deleteIcon.setForeground(new Color(220, 60, 60));
        actionIconsPanel.add(deleteIcon);

        itemPanel.add(actionIconsPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 0, 0);

        JLabel descLabel = new JLabel("<html><body style='width: 500px;'><p style='color: #444;'>" + description + "</p></body></html>");
        itemPanel.add(descLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 0, 0);

        JLabel urlLabel = new JLabel("<html><a href='" + url + "' style='color: #0066cc; text-decoration: none;'>" + url + "</a></html>");
        itemPanel.add(urlLabel, gbc);

        return itemPanel;
    }

    @Override
    public void onStateChanged() {
        // TODO
    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
