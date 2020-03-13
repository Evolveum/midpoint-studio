package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.impl.trace.PerformanceCategory;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.evolveum.midpoint.studio.util.MidPointUtils.formatPercent;
import static com.evolveum.midpoint.studio.util.MidPointUtils.formatTime;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceViewPanel extends JPanel {

    private JXTreeTable main;

    private JXTreeTable traceStructure;

    public TraceViewPanel(List<OpNode> data) {
        super(new BorderLayout());

        initLayout(data);
    }

    private void initLayout(List<OpNode> data) {
        JBSplitter horizontal = new OnePixelSplitter(true);
        add(horizontal, BorderLayout.CENTER);

        JComponent main = initMain(data);
        horizontal.setFirstComponent(main);

        JBSplitter horizontal2 = new OnePixelSplitter(true);
        horizontal.setSecondComponent(horizontal2);

        JComponent traceStructure = initTraceStructure(data);
        horizontal2.setFirstComponent(traceStructure);

        JComponent tracePerformance = initTracePerformance();
        horizontal2.setSecondComponent(tracePerformance);
    }

    private JComponent initMain(List<OpNode> data) {
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

        main = MidPointUtils.createTable(new TraceTreeTableModel(columns, data), columns);
        main.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        main.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                Object obj = path.getLastPathComponent();

                // todo
            }
        });

        return new JBScrollPane(main);
    }

    private JComponent initTraceStructure(List<OpNode> data) {
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

        // todo add columns

        columns.add(new TreeTableColumnDefinition<OpNode, String>("Log", 50, o -> Integer.toString(o.getLogEntriesCount())));

        traceStructure = MidPointUtils.createTable(new TraceStructureTreeTableModel(columns, data), columns);

        return new HeaderDecorator("Trace Structure", new JBScrollPane(traceStructure));
    }

    private JComponent initTracePerformance() {
        TracePerformanceInformationPanel perfInformation = new TracePerformanceInformationPanel();
        return new HeaderDecorator("Trace Performance Information", new JBScrollPane(perfInformation));
    }

    private void addPerformanceColumn(List<TreeTableColumnDefinition> columns, PerformanceCategory category, boolean hidable, boolean readWrite) {
        TreeTableColumnDefinition def = new TreeTableColumnDefinition<OpNode, Integer>(category.getShortLabel() + " #", 70, o -> o.getPerformanceByCategory().get(category).getTotalCount());
        def.tableCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(getColor((int) value));

                return c;
            }
        });
        columns.add(def);

//        if (readWrite) {
//            readWriteOpColumns.add(countColumn);
//        } else if (hidable) {
//            hidablePerformanceColumns.add(countColumn);
//        }
//        //countColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getPerformanceByCategory().get(category).getTotalCount())));

        columns.add(new TreeTableColumnDefinition<OpNode, String>(category.getShortLabel() + " time", 80, o -> formatTime(o.getPerformanceByCategory().get(category).getTotalTime())));

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

    private static Color getColor(int value) {
        if (value != 0) {
            return JBUI.CurrentTheme.Label.foreground();
        } else {
            return JBUI.CurrentTheme.Label.disabledForeground();
        }
    }
}
