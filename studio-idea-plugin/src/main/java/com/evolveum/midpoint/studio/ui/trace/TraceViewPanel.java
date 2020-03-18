package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.impl.trace.Options;
import com.evolveum.midpoint.studio.impl.trace.PerformanceCategory;
import com.evolveum.midpoint.studio.impl.trace.TraceManager;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
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

    private static final Logger LOG = Logger.getInstance(TraceViewPanel.class);

    private JXTreeTable main;

    private JXTreeTable traceStructure;

    private MidPointProjectNotifier notifier;

    private List<OpNode> data;

    private List<TreeTableColumnDefinition> hideablePerformanceColumns = new ArrayList<>();

    private List<TreeTableColumnDefinition> readWriteOpColumns = new ArrayList<>();

    public TraceViewPanel(Project project, List<OpNode> data) {
        super(new BorderLayout());

        this.data = data;

        MessageBus bus = project.getMessageBus();
        this.notifier = bus.syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC);

        initLayout(bus);

        TraceManager tm = TraceManager.getInstance(project);
        applyOptions(tm.getOptions());
    }

    private void initLayout(MessageBus bus) {
        JBSplitter horizontal = new OnePixelSplitter(true);
        add(horizontal, BorderLayout.CENTER);

        JComponent main = initMain(data);
        horizontal.setFirstComponent(main);

        JBSplitter horizontal2 = new OnePixelSplitter(true);
        horizontal.setSecondComponent(horizontal2);

        JComponent traceStructure = initTraceStructure(data);
        horizontal2.setFirstComponent(traceStructure);

        JComponent tracePerformance = initTracePerformance(bus);
        horizontal2.setSecondComponent(tracePerformance);

//        JXTreeTable t = new JXTreeTable();
//        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        DefaultMutableTreeTableNode n1 = new DefaultMutableTreeTableNode("a");
//        n1.add(new DefaultMutableTreeTableNode("b"));
//        n1.add(new DefaultMutableTreeTableNode("c"));
//        t.setTreeTableModel(new DefaultTreeTableModel(n1) {
//
//            @Override
//            public int getColumnCount() {
//                return 3;
//            }
//
//            @Override
//            public String getColumnName(int column) {
//                return Integer.toString(column);
//            }
//
//            @Override
//            public Object getValueAt(Object node, int column) {
//                return node.toString() + column;
//            }
//        });
//        t.addTreeSelectionListener(new TreeSelectionListener() {
//
//            @Override
//            public void valueChanged(TreeSelectionEvent e) {
//                System.out.println("");
//            }
//        });
////        add(t, BorderLayout.SOUTH);
//        horizontal.setSecondComponent(new JBScrollPane(t));
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
        main.addTreeSelectionListener(e -> mainTableSelectionChanged(e));

        return new JBScrollPane(main);
    }

    private void mainTableSelectionChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) path.getLastPathComponent();

        OpNode opNode = node != null ? (OpNode) node.getUserObject() : null;

        notifier.selectedTraceNodeChange(opNode);
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

    private JComponent initTracePerformance(MessageBus bus) {
        TracePerformanceInformationPanel perfInformation = new TracePerformanceInformationPanel(bus);
        return new HeaderDecorator("Trace Performance Information", new JBScrollPane(perfInformation));
    }

    private void addPerformanceColumn(List<TreeTableColumnDefinition> columns, PerformanceCategory category, boolean hideable, boolean readWrite) {
        TreeTableColumnDefinition count = new TreeTableColumnDefinition<OpNode, Integer>(
                category.getShortLabel() + " #",
                70,
                o -> o.getPerformanceByCategory().get(category).getTotalCount());
        count.tableCellRenderer(new ColoredTableCellRenderer());

        columns.add(count);

        if (readWrite) {
            readWriteOpColumns.add(count);
        } else if (hideable) {
            hideablePerformanceColumns.add(count);
        }

        TreeTableColumnDefinition time = new TreeTableColumnDefinition<OpNode, String>(
                category.getShortLabel() + " time",
                80,
                o -> formatTime(o.getPerformanceByCategory().get(category).getTotalTime()));
        time.tableCellRenderer(new ColoredTableCellRenderer());

        columns.add(time);

        if (readWrite) {
            readWriteOpColumns.add(time);
        } else {
            hideablePerformanceColumns.add(time);
        }
    }

    private static Color getColor(Object value) {
        if (value == null || value.toString() == "0") {
            return JBUI.CurrentTheme.Label.disabledForeground();
        }

        return JBUI.CurrentTheme.Label.foreground();
    }

    public void applyOptions(Options options) {
        LOG.debug("Applying options", options);

        if (data == null) {
            return;
        }

        for (OpNode root : data) {
            root.applyOptions(options);
        }

        for (TreeTableColumnDefinition column : hideablePerformanceColumns) {
            column.setVisible(options.isShowPerformanceColumns());
        }
        for (TreeTableColumnDefinition column : readWriteOpColumns) {
            column.setVisible(options.isShowReadWriteColumns());
        }

        TraceTreeTableModel model = (TraceTreeTableModel) main.getTreeTableModel();

        DefaultTableColumnModelExt columnModel = (DefaultTableColumnModelExt) main.getColumnModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumnExt tce = columnModel.getColumnExt(i);
            tce.setVisible(model.getColumn(i).isVisible());
        }
    }

    private static class ColoredTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setForeground(getColor(value));

            return c;
        }
    }
}
