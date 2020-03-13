package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceLensContextPanel extends BorderLayoutPanel {

    private JLabel label;
    private JXTreeTable table;

    public TraceLensContextPanel() {
        initLayout();
    }

    private void initLayout() {
        label = new JLabel();
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        add(label, BorderLayout.CENTER);

        List<TreeTableColumnDefinition> columns = new ArrayList<>();

        columns.add(new TreeTableColumnDefinition<String, String>("Item", 500, o -> null));
        columns.add(new TreeTableColumnDefinition<String, String>("Old", 500, o -> null));
        columns.add(new TreeTableColumnDefinition<String, String>("Current", 500, o -> null));
        columns.add(new TreeTableColumnDefinition<String, String>("New", 500, o -> null));

        this.table = MidPointUtils.createTable(new DefaultTreeTableModel(new DefaultMutableTreeTableNode("")), null);

        add(new JBScrollPane(table), BorderLayout.CENTER);
    }
}
