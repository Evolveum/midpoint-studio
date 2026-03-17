package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.objectClass;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SchemaValidationPanel extends JBPanel<DocumentationPanel> implements WizardContent {

    public SchemaValidationPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(20));

        JBPanel<?> header = new JBPanel<>(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 5, true, false));
        header.add(new JBLabel("Review Schema Attributes") {{ setFont(JBUI.Fonts.label(18f)); }});
        header.add(new JBLabel("View the object schema attributes which were extracted from the schema script...") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});
        add(header, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setBorder(JBUI.Borders.emptyTop(20));

        JBPanel<?> sidebar = new JBPanel<>(new BorderLayout());
        SearchTextField searchField = new SearchTextField();
        sidebar.add(searchField, BorderLayout.NORTH);

        JBList<String> objectList = new JBList<>(new String[]{"User", "Account", "Group"});
        objectList.setSelectedIndex(0);
        sidebar.add(new JScrollPane(objectList), BorderLayout.CENTER);

        splitPane.setLeftComponent(sidebar);

        JBTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Items 13", AllIcons.General.LayoutEditorPreview, createItemsTab());
        tabbedPane.addTab("Definition settings", AllIcons.General.Settings, new JPanel());

        splitPane.setRightComponent(tabbedPane);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createItemsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.getPanelBackground());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterBar.add(new JBLabel("Display name") {{ setIcon(AllIcons.General.ContextHelp); }});
        filterBar.add(new JTextField(12));
        filterBar.add(new JBLabel("Name") {{ setIcon(AllIcons.General.ContextHelp); }});
        filterBar.add(new JTextField(12));
        filterBar.add(new JButton("Basic", AllIcons.General.Filter));
        panel.add(filterBar, BorderLayout.NORTH);

        String[] columnNames = {"Name", "Type", "Display name", "Order", "Required", "Multivalue", "Indexed", ""};
        Object[][] data = {
                {"language", "String", "", "120", "False", "False", "", AllIcons.Actions.Edit},
                {"updatedAt", "Date time", "", "130", "False", "False", "", AllIcons.Actions.Edit},
                {"firstName", "String", "", "140", "False", "False", "", AllIcons.Actions.Edit},
                {"email", "String", "", "150", "False", "False", "", AllIcons.Actions.Edit},
                {"status", "String", "", "170", "False", "False", "", AllIcons.Actions.Edit}
        };

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return (column == 7) ? Icon.class : String.class;
            }
        };

        JBTable table = new JBTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
