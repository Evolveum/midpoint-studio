package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevAuthInfoType;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
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

public class AuthMethodSupport {

    ConnectorGeneratorDataModel dataModel;

    private JPanel content;
    private JPanel header;
    private JLabel text;
    private JTextPane subtext;
    private JPanel mainPanel;
    private JPanel listItemContainerPanel;
    private JScrollPane listItemScrollPanel;
    private JPanel itemPanel;
    private JPanel recommendedOptionsBtnWrapper;
    private JToggleButton recommendedOptionsBtn;

    private Set<ConnDevAuthInfoType> selectedItem = new HashSet<>();

    public AuthMethodSupport(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
        initComponents();
    }

    private void initComponents() {
        getItemPanel().setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        getListItemScrollPanel().setViewportView(itemPanel);

        recommendedOptionsBtn = new JToggleButton();
        recommendedOptionsBtn.setBorder(JBUI.Borders.empty());

        if (recommendedOptionsBtnWrapper != null) {
            recommendedOptionsBtnWrapper.add(recommendedOptionsBtn);
        }
    }

    public void fillItemList(@NotNull List<ConnDevAuthInfoType> items) {

        itemPanel.removeAll();

        for (ConnDevAuthInfoType item : items) {
            var itemRow = new AuthMethodSupport.ItemRowComponent(item, getSelectedItem());
            itemRow.selected(dataModel.connectorDevelopmentType.getApplication().getAuth().contains(item));
            itemPanel.add(itemRow);
        }
    }

    public JToggleButton getRecommendedOptionsBtn() {
        return recommendedOptionsBtn;
    }

    public Set<ConnDevAuthInfoType> getSelectedItem() {
        return selectedItem;
    }

    public JPanel getContent() {
        return content;
    }

    public JPanel getHeader() {
        return header;
    }

    public JLabel getText() {
        return text;
    }

    public JTextPane getSubtext() {
        return subtext;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JPanel getListItemContainerPanel() {
        return listItemContainerPanel;
    }

    public JScrollPane getListItemScrollPanel() {
        return listItemScrollPanel;
    }

    public JPanel getItemPanel() {
        return itemPanel;
    }

    public static class ItemRowComponent extends JPanel {

        private final JBCheckBox checkBox;
        private final JBLabel title;
        private final JTextPane description;

        public ItemRowComponent(ConnDevAuthInfoType connDevAuthInfoTypeItem,
                                Set<ConnDevAuthInfoType> selectedConnDevAuthInfoType
        ) {
            super(new BorderLayout());

            setBorder(getBorderOfItem(JBColor.GRAY));
            setOpaque(false);

            checkBox = new JBCheckBox();
            checkBox.addActionListener(e -> {
                boolean checked = checkBox.isSelected();
                setBorder(getBorderOfItem(checked ? JBColor.BLUE : JBColor.GRAY));

                if (checked) {
                    selectedConnDevAuthInfoType.add(connDevAuthInfoTypeItem);
                } else {
                    selectedConnDevAuthInfoType.remove(connDevAuthInfoTypeItem);
                }
            });
            add(checkBox, BorderLayout.WEST);

            JPanel contentPanel = new JPanel(new GridLayout(3, 1));
            contentPanel.setOpaque(false);

            title = new JBLabel(connDevAuthInfoTypeItem.getName());
            title.setFont(UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(Font.BOLD));
            title.setBorder(new EmptyBorder(JBUI.insets(0, 10)));

            JPanel titleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            titleWrapper.setOpaque(false);
            titleWrapper.add(title);


            if (connDevAuthInfoTypeItem.isRecommended() != null && connDevAuthInfoTypeItem.isRecommended())
                titleWrapper.add(new GenerateConnectorBadge(GenerateConnectorBadge.Recommended.RECOMMENDED));
            contentPanel.add(titleWrapper);

            description = new JTextPane();
            description.setEditable(false);
            description.setOpaque(false);
            description.setForeground(UIUtil.getContextHelpForeground());
            description.setFocusable(false);
            description.setBorder(new EmptyBorder(JBUI.insets(0, 10)));
            description.setText(connDevAuthInfoTypeItem.getDescription());
            contentPanel.add(description);

            add(contentPanel, BorderLayout.CENTER);
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
