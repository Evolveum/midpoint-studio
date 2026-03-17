package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.objectClass;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SearchResultPanel extends JBPanel<DocumentationPanel> implements WizardContent {

    public SearchResultPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(20));

        JBPanel<?> headerPanel = new JBPanel<>(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 10, true, false));
        headerPanel.add(new JBLabel("Validate Search Results") {{
            setFont(JBUI.Fonts.label(18f));
        }});
        headerPanel.add(new JBLabel("<html>Below is a table with data retrieved using the generated search script. Review the results to ensure the correct " +
                "attributes are present and values are returned as expected. If everything looks good, you can proceed and validate to finalize the configuration.</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});
        add(headerPanel, BorderLayout.NORTH);

        String[] columnNames = {"", "Name", "Identifiers", "Situation", "Owner", "Pending operations"};
        Object[][] data = {
                {false, "ilvy", "<html>login: ilvy<br>id: 2324</html>", "", "", ""},
                {false, "ibuggs", "<html>login: ibuggs<br>id: 2323</html>", "", "", ""},
                {false, "ssteel", "<html>login: ssteel<br>id: 2322</html>", "", "", ""},
                {false, "jfarbermeister", "<html>login: jfarbermeister<br>id: 2321</html>", "", "", ""},
                {false, "jsamuelson", "<html>login: jsamuelson<br>id: 2320</html>", "", "", ""}
        };

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return (column == 0) ? Boolean.class : String.class;
            }
        };

        JBTable table = new JBTable(model);

        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getColumnModel().getColumn(0).setMaxWidth(40);

        table.getColumnModel().getColumn(0).setCellRenderer(new BooleanTableCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameWithIconRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new MultiLineRenderer());

        JBScrollPane scrollPane = new JBScrollPane(table);
        scrollPane.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    private static class NameWithIconRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JBLabel label = (JBLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setIcon(AllIcons.General.User);
            label.setIconTextGap(10);
            label.setBorder(JBUI.Borders.emptyLeft(10));
            return label;
        }
    }

    private static class MultiLineRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JBLabel label = (JBLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setForeground(UIUtil.getContextHelpForeground());
            label.setFont(JBUI.Fonts.smallFont());
            return label;
        }
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
