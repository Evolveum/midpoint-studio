package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.impl.trace.PerformanceCategory;
import com.intellij.ui.components.JBScrollPane;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceViewPanel extends JPanel {

    private JXTreeTable table;

    public TraceViewPanel(List<OpNode> data) {
        super(new BorderLayout());

        initLayout(data);
    }

    private void initLayout(List<OpNode> data) {
        List<TreeTableColumnDefinition> columns = new ArrayList<>();
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Operation", 500, o -> o.getOperationNameFormatted()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("State", 60, o -> o.getClockworkState()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("EW", 35, o -> o.getExecutionWave()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("PW", 35, o -> o.getProjectionWave()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Status", 100, o -> o.getResult().getStatus().toString()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("W", 20, o -> o.getImportanceSymbol()));

        long start = System.currentTimeMillis();    // todo fix
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Start", 60, o -> Long.toString(o.getStart(start))));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Time", 80, o -> formatTime(o.getResult().getMicroseconds())));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Type", 100, o -> o.getType().toString()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("OH", 50, o -> formatPercent(o.getOverhead())));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("OH2", 50, o -> formatPercent(o.getOverhead2())));

        addPerformanceColumn(columns, PerformanceCategory.REPOSITORY, false, false);
        addPerformanceColumn(columns, PerformanceCategory.REPOSITORY_READ, false, true);
        addPerformanceColumn(columns, PerformanceCategory.REPOSITORY_WRITE, false, true);
        addPerformanceColumn(columns, PerformanceCategory.REPOSITORY_CACHE, true, false);
        addPerformanceColumn(columns, PerformanceCategory.MAPPING_EVALUATION, false, false);
        addPerformanceColumn(columns, PerformanceCategory.ICF, false, false);
        addPerformanceColumn(columns, PerformanceCategory.ICF_READ, false, true);
        addPerformanceColumn(columns, PerformanceCategory.ICF_WRITE, false, true);

        columns.add(new TreeTableColumnDefinition<OpNode, String>("Log", 50, o -> Integer.toString(o.getLogEntriesCount())));

        table = new JXTreeTable(new TraceTreeTableModel(columns, data));
        table.setRootVisible(false);

        for (int i = 0; i < columns.size(); i++) {
            TreeTableColumnDefinition def = columns.get(i);
            table.getColumnModel().getColumn(i).setMinWidth(def.getSize());
            table.getColumnModel().getColumn(i).setPreferredWidth(def.getSize());
        }

        JBScrollPane pane = new JBScrollPane(table);
        add(pane, BorderLayout.CENTER);
    }

    private void addPerformanceColumn(List<TreeTableColumnDefinition> columns, PerformanceCategory category, boolean hidable, boolean readWrite) {
        columns.add(new TreeTableColumnDefinition<OpNode, String>(category.getShortLabel() + " #", 70, o -> Integer.toString(o.getPerformanceByCategory().get(category).getTotalCount())));

//        countColumn.setLabelProvider(new ColumnLabelProvider() {
//            @Override
//            public String getText(Object element) {
//                return String.valueOf(getCount(element));
//            }
//
//            private int getCount(Object element) {
//                return ((OpNode) element).getPerformanceByCategory().get(category).getTotalCount();
//            }
//
//            @Override
//            public Color getForeground(Object element) {
//                return TracePerformanceView.getColor(getCount(element));
//            }
//        });
//        if (readWrite) {
//            readWriteOpColumns.add(countColumn);
//        } else if (hidable) {
//            hidablePerformanceColumns.add(countColumn);
//        }
//        //countColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getPerformanceByCategory().get(category).getTotalCount())));

        columns.add(new TreeTableColumnDefinition<OpNode, String>(category.getShortLabel() + " time", 80, o -> formatTime(o.getPerformanceByCategory().get(category).getTotalTime())));

//        TreeViewerColumn timeColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
//        timeColumn.getColumn().setWidth(80);
//        timeColumn.getColumn().setText(category.getShortLabel() + " time");
//        timeColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> formatTime(n.getPerformanceByCategory().get(category).getTotalTime()))));
//        timeColumn.setLabelProvider(new ColumnLabelProvider() {
//            @Override
//            public String getText(Object element) {
//                return formatTime(getTime(element));
//            }
//
//            private long getTime(Object element) {
//                return ((OpNode) element).getPerformanceByCategory().get(category).getTotalTime();
//            }
//
//            @Override
//            public Color getForeground(Object element) {
//                return TracePerformanceView.getColor(getTime(element));
//            }
//        });
//        if (readWrite) {
//            readWriteOpColumns.add(countColumn);
//        } else {
//            hidablePerformanceColumns.add(timeColumn);
//        }
    }

    private static String formatTime(Long time) {
        if (time == null) {
            return "";
        } else {
            return String.format(Locale.US, "%.1f", time / 1000.0);
        }
    }

    private static String formatPercent(Double value) {
        if (value == null) {
            return "";
        } else {
            return String.format(Locale.US, "%.1f%%", value * 100);
        }
    }
}
