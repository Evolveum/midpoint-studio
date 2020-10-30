package com.evolveum.midpoint.studio.ui.performance.mainTree;

import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;
import com.evolveum.midpoint.studio.impl.performance.PerformanceOptions;
import com.evolveum.midpoint.studio.impl.performance.PerformanceTree;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.ui.performance.mainTree.model.PerformanceTreeTableModel;
import com.evolveum.midpoint.studio.ui.performance.mainTree.model.PerformanceTreeTableNode;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class PerformanceTreePanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(PerformanceTreePanel.class);

    private JXTreeTable treeTable;

    private PerformanceTreeTableModel treeTableModel;

    private final MidPointProjectNotifier notifier;

    private PerformanceOptions lastOptions;

    @Nullable
    private final PerformanceTree performanceTree;

    private final List<TreeTableColumnDefinition<OperationPerformance, ?>> columnDefinitions = new ArrayList<>();

    public PerformanceTreePanel(Project project, @Nullable PerformanceTree performanceTree) {
        super(new BorderLayout());

        this.performanceTree = performanceTree;

        MessageBus bus = project.getMessageBus();
        this.notifier = bus.syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC);

        initLayout();
    }

    private void initLayout() {
        add(initMain());
    }

    private JComponent initMain() {

        for (PerformanceTreeViewColumn column : PerformanceTreeViewColumn.values()) {
            columnDefinitions.add(new TreeTableColumnDefinition<>(column));
        }

        treeTableModel = new PerformanceTreeTableModel(columnDefinitions, performanceTree != null ? performanceTree.getRoot() : null);

        treeTable = MidPointUtils.createTable2(treeTableModel, MidPointUtils.createTableColumnModel(columnDefinitions), false,
                table -> table.setRootVisible(true)
        );

        treeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        treeTable.addTreeSelectionListener(this::traceTreeTableSelectionChanged);

        JPanel panel = new BorderLayoutPanel();
        JComponent toolbar = initMainToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        panel.add(new JBScrollPane(treeTable), BorderLayout.CENTER);

        return panel;
    }


    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> treeTable.expandAll()));
        group.add(MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> treeTable.collapseAll()));

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("PerformanceViewPanelMainToolbar", group, true);
        return resultsActionsToolbar.getComponent();
    }

    private void traceTreeTableSelectionChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        System.out.println("PerformanceTreePanel: traceTreeTableSelectionChanged: " + path);
        selectedNodeChange(path);
    }

    public void selectedNodeChange(TreePath path) {
        if (path != null) {
            PerformanceTreeTableNode node = (PerformanceTreeTableNode) path.getLastPathComponent();
            notifier.selectedPerformanceNodeChange(node != null ? node.getUserObject() : null);
        } else {
            notifier.selectedPerformanceNodeChange(null);
        }
    }

    public void selectNotify() {
        int index = treeTable.getSelectionModel().getLeadSelectionIndex();
        System.out.println("PerformanceTreePanel: selectNotify: " + index);
        if (index >= 0) {
            TreePath path = treeTable.getPathForRow(index);
            selectedNodeChange(path);
        } else {
            notifier.selectedPerformanceNodeChange(null);
        }
    }

    public void deselectNotify() {
        selectedNodeChange(null);
    }

    public void applyOptions(PerformanceOptions options) {
        LOG.debug("Applying options", options);

        DefaultTableColumnModelExt realColumnModel = (DefaultTableColumnModelExt) treeTable.getColumnModel();
        List<TableColumn> realColumns = realColumnModel.getColumns(true);
        for (int i = 0; i < columnDefinitions.size(); i++) {
            TreeTableColumnDefinition<OperationPerformance, ?> columnDef = columnDefinitions.get(i);
            Object identifier = realColumns.get(i).getIdentifier();
            TableColumnExt realColumnExt = realColumnModel.getColumnExt(identifier);

            //noinspection SuspiciousMethodCalls
            boolean visible = options.getColumnsToShow().contains(columnDef.getOriginalColumnDefinition());
            columnDef.setVisible(visible);
            realColumnExt.setVisible(visible);
        }

        lastOptions = options.clone();
    }
}
