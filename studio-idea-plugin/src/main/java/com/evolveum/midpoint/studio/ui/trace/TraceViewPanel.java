package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.impl.trace.Options;
import com.evolveum.midpoint.studio.impl.trace.PerformanceCategory;
import com.evolveum.midpoint.studio.impl.trace.TraceManager;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
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

    private JLabel traceStructureLabel;

    private JXTreeTable traceStructure;

    private MidPointProjectNotifier notifier;

    private List<OpNode> data;

    private List<TreeTableColumnDefinition> hideablePerformanceColumns = new ArrayList<>();

    private List<TreeTableColumnDefinition> readWriteOpColumns = new ArrayList<>();

    public TraceViewPanel(Project project, List<OpNode> data, long startTimestamp) {
        super(new BorderLayout());

        this.data = data;

        MessageBus bus = project.getMessageBus();
        this.notifier = bus.syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC);

        initLayout(bus, startTimestamp);

        TraceManager tm = TraceManager.getInstance(project);
        applyOptions(tm.getOptions());
    }

    private void initLayout(MessageBus bus, long startTimestamp) {
        JBSplitter horizontal = new OnePixelSplitter(true);
        add(horizontal, BorderLayout.CENTER);

        JComponent main = initMain(data, startTimestamp);
        horizontal.setFirstComponent(main);

        JBSplitter horizontal2 = new OnePixelSplitter(true);
        horizontal.setSecondComponent(horizontal2);

        JComponent traceStructure = initTraceStructure(data, startTimestamp);
        horizontal2.setFirstComponent(traceStructure);

        JComponent tracePerformance = initTracePerformance(bus);
        horizontal2.setSecondComponent(tracePerformance);
    }

    private JComponent initMain(List<OpNode> data, long start) {
        List<TreeTableColumnDefinition> columns = new ArrayList<>();

        columns.add(new TreeTableColumnDefinition<OpNode, String>("Operation", 500, o -> o.getOperationNameFormatted()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("State", 60, o -> o.getClockworkState()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("EW", 35, o -> o.getExecutionWave()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("PW", 35, o -> o.getProjectionWave()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Status", 100, o -> o.getResult().getStatus().toString()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("W", 20, o -> o.getImportanceSymbol()));

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

        JPanel panel = new BorderLayoutPanel();
        JComponent toolbar = initMainToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        panel.add(new JBScrollPane(main), BorderLayout.CENTER);

        return panel;
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> main.expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Collapseall, e -> main.collapseAll());
        group.add(collapseAll);

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("TraceViewPanelMainToolbar", group, true);
        return resultsActionsToolbar.getComponent();
    }

    private JComponent initTraceStructureToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> traceStructure.expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> traceStructure.collapseAll());
        group.add(collapseAll);

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("TraceViewPanelStructureToolbar", group, true);
        return resultsActionsToolbar.getComponent();
    }

    private void mainTableSelectionChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) path.getLastPathComponent();

        OpNode opNode = node != null ? (OpNode) node.getUserObject() : null;

        notifier.selectedTraceNodeChange(opNode);
    }

    private JComponent initTraceStructure(List<OpNode> data, long start) {
        List<TreeTableColumnDefinition> columns = new ArrayList<>();

        columns.add(new TreeTableColumnDefinition<OpNode, String>("Operation", 500, o -> o.getOperationNameFormatted()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("State", 60, o -> o.getClockworkState()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("EW", 35, o -> o.getExecutionWave()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("PW", 35, o -> o.getProjectionWave()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Status", 100, o -> o.getResult().getStatus().toString()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("W", 20, o -> o.getImportanceSymbol()));

        columns.add(new TreeTableColumnDefinition<OpNode, String>("Start", 60, o -> Long.toString(o.getStart(start))));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Time", 80, o -> formatTime(o.getResult().getMicroseconds())));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Type", 100, o -> o.getType().toString()));

        // todo add columns

        columns.add(new TreeTableColumnDefinition<OpNode, String>("Log", 50, o -> Integer.toString(o.getLogEntriesCount())));

        traceStructure = MidPointUtils.createTable(new TraceStructureTreeTableModel(columns, data), columns);

        JComponent toolbar = initTraceStructureToolbar();

        traceStructureLabel = new JLabel();
        traceStructureLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        refreshFileLabel(null);

        JPanel panel = MidPointUtils.createBorderLayoutPanel(
                MidPointUtils.createBorderLayoutPanel(toolbar, traceStructureLabel, null),
                new JBScrollPane(traceStructure),
                null
        );

        return new HeaderDecorator("Trace Structure", panel);
    }

    private JComponent initTracePerformance(MessageBus bus) {
        TracePerformanceInformationPanel perfInformation = new TracePerformanceInformationPanel(bus);
        return new HeaderDecorator("Trace Performance Information", new JBScrollPane(perfInformation));
    }

    private void refreshFileLabel(String traceFile) {
        traceStructureLabel.setText("Trace file: " + (traceFile != null ? traceFile : "(undefined)"));
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
