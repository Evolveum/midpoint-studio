package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.ui.connector.generator.component.AiAlertPanel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDocumentationSourceType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiscoverDocumentation {

    private ConnectorGeneratorDataModel dataModel;

    private JPanel mainPanel;
    private JTextPane subtext;
    private JLabel text;
    private JPanel header;
    private JPanel actionDocsPanel;
    private JButton uploadFileButton;
    private JButton addUrlButton;
    private JButton createNewButton;
    private JPanel content;
    private JPanel listDocsContainer;
    private JScrollPane listItemScrollPanel;
    private JPanel aiAlert;
    private JPanel itemDocPanel;
    private JCheckBox allCheckBox;
    private JScrollPane listDocsScrollPanel;

    private Set<ConnDevDocumentationSourceType> selectedDocumentationSources = new HashSet<>();

    private final StatusPanel statusPanel = new StatusPanel();

    public DiscoverDocumentation(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
        initComponents();
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public JLabel getText() {
        return text;
    }

    public void setText(JLabel text) {
        this.text = text;
    }

    public JPanel getHeader() {
        return header;
    }

    public void setHeader(JPanel header) {
        this.header = header;
    }

    public JPanel getContent() {
        return content;
    }

    public void setContent(JPanel content) {
        this.content = content;
    }

    public JTextPane getSubtext() {
        return subtext;
    }

    public void setSubtext(JTextPane subtext) {
        this.subtext = subtext;
    }

    public JPanel getActionDocsPanel() {
        return actionDocsPanel;
    }

    public void setActionDocsPanel(JPanel actionDocsPanel) {
        this.actionDocsPanel = actionDocsPanel;
    }

    public JButton getUploadFileButton() {
        return uploadFileButton;
    }

    public void setUploadFileButton(JButton uploadFileButton) {
        this.uploadFileButton = uploadFileButton;
    }

    public JButton getAddUrlButton() {
        return addUrlButton;
    }

    public void setAddUrlButton(JButton addUrlButton) {
        this.addUrlButton = addUrlButton;
    }

    public JButton getCreateNewButton() {
        return createNewButton;
    }

    public void setCreateNewButton(JButton createNewButton) {
        this.createNewButton = createNewButton;
    }

    public JPanel getListDocsContainer() {
        return listDocsContainer;
    }

    public void setListDocsContainer(JPanel listDocsContainer) {
        this.listDocsContainer = listDocsContainer;
    }

    public JScrollPane getListDocsScrollPanel() {
        return listItemScrollPanel;
    }

    public void setListDocsScrollPanel(JScrollPane listDocsScrollPanel) {
        this.listItemScrollPanel = listDocsScrollPanel;
    }

    public JPanel getItemDocPanel() {
        return itemDocPanel;
    }

    public void setItemDocPanel(JPanel itemDocPanel) {
        this.itemDocPanel = itemDocPanel;
    }

    public Set<ConnDevDocumentationSourceType> getSelectedDocumentationSources() {
        return selectedDocumentationSources;
    }

    private void initComponents() {
        itemDocPanel.setLayout(new BoxLayout(itemDocPanel, BoxLayout.Y_AXIS));
        listDocsScrollPanel.setViewportView(itemDocPanel);
    }

    public void fillDocumentationList(@NotNull List<ConnDevDocumentationSourceType> documentation) {

        itemDocPanel.removeAll();

        for (ConnDevDocumentationSourceType documentationSourceType : documentation) {
            var itemDocumentationSource = new ItemRowComponent(documentationSourceType, getSelectedDocumentationSources());
            itemDocumentationSource.selected(dataModel.connectorDevelopmentType.getDocumentationSource().contains(documentationSourceType));
            itemDocPanel.add(itemDocumentationSource);
        }
    }

    private void createUIComponents() {
        aiAlert = new AiAlertPanel(new BorderLayout(), 15, "Documentation found", """
                AI has found matching documentation or configurations. Please review them to ensure they fit your needs..
                """);
    }

    public static class ItemRowComponent extends JPanel {

        private final JBCheckBox checkBox;
        private final JBLabel title;
        private final JTextPane description;
        private final LinkLabel<?> uriLink;
        private final JButton viewButton = new JButton(AllIcons.Actions.Preview);
        private final JButton deleteButton = new JButton(AllIcons.Actions.GC);

        public ItemRowComponent(ConnDevDocumentationSourceType documentationSourceType,
                                Set<ConnDevDocumentationSourceType> selectedDocumentationSources
        ) {
            super(new BorderLayout());

            setBorder(getBorderOfItem(JBColor.GRAY));
            setOpaque(false);

            checkBox = new JBCheckBox();
            checkBox.addActionListener(e -> {
                boolean checked = checkBox.isSelected();
                setBorder(getBorderOfItem(checked ? JBColor.BLUE : JBColor.GRAY));

                if (checked) {
                    selectedDocumentationSources.add(documentationSourceType);
                } else {
                    selectedDocumentationSources.remove(documentationSourceType);
                }
            });
            add(checkBox, BorderLayout.WEST);

            JPanel contentPanel = new JPanel(new GridLayout(3, 1));
            contentPanel.setOpaque(false);

            title = new JBLabel(documentationSourceType.getName());
            title.setFont(UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(Font.BOLD));
            title.setBorder(new EmptyBorder(JBUI.insets(0, 10)));
            contentPanel.add(title);

            description = new JTextPane();
            description.setEditable(false);
            description.setOpaque(false);
            description.setForeground(UIUtil.getContextHelpForeground());
            description.setFocusable(false);
            description.setBorder(new EmptyBorder(JBUI.insets(0, 10)));
            description.setText(documentationSourceType.getDescription());
            contentPanel.add(description);

            uriLink = new LinkLabel<>(
                    documentationSourceType.getUri(),
                    null,
                    (link, data) -> BrowserUtil.browse(documentationSourceType.getUri()));
            uriLink.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
            uriLink.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(uriLink);

            JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            actionButtonPanel.setOpaque(false);
            actionButtonPanel.add(viewButton);
            actionButtonPanel.add(deleteButton);

            add(contentPanel, BorderLayout.CENTER);
            add(actionButtonPanel, BorderLayout.EAST);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension superPref = super.getPreferredSize();
            Container parent = this.getParent();

            if (parent != null) {
                int parentWidth = parent.getWidth() - JBUI.scale(24);
                if (parentWidth > 0) {
                    return new Dimension(parentWidth, superPref.height);
                }
            }
            return superPref;
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        private Border getBorderOfItem(Color color) {
            var innerPadding = JBUI.Borders.empty(12, 16);
            var lineAndPadding = BorderFactory.createCompoundBorder(JBUI.Borders.customLine(
                            color, 1, 1, 1, 1),
                    innerPadding);
            return BorderFactory.createCompoundBorder(
                    JBUI.Borders.emptyBottom(10),
                    lineAndPadding);
        }

        private void selected(boolean selected) {
            checkBox.setSelected(selected);
            this.revalidate();
            this.repaint();
        }
    }
}
