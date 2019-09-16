package com.evolveum.midpoint.studio.ui;

import com.intellij.ui.table.JBTable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class QueryResultsPanel extends JPanel {

    private JPanel panel;
    private JTable table;
    private PagingPanel paging;
    private JScrollPane scrollPane;

    public QueryResultsPanel() {
        super(new BorderLayout());
    }

    private void createUIComponents() {
        panel = new JPanel();
        add(panel, BorderLayout.CENTER);

        table = new JBTable(new BrowseTableModel());
        table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public JTable getTable() {
        return table;
    }

    public BrowseTableModel getTableModel() {
        return (BrowseTableModel) table.getModel();
    }

    public List<String> getSelectedRowsOids() {
        int[] rows = table.getSelectedRows();

        List<String> oids = new ArrayList<>();
        for (int index : rows) {
            oids.add(getTableModel().getData().get(index).getOid());
        }

        return oids;
    }
}
