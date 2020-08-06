package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.trace.Options;
import com.evolveum.midpoint.studio.impl.trace.TraceManager;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.ui.trace.lens.TraceTreeViewColumn;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
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
 * Created by Viliam Repan (lazyman).
 */
public class TraceTreeViewPanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(TraceTreeViewPanel.class);

    private JXTreeTable main;

    private TraceTreeTableModel traceTreeTableModel;

    private final MidPointProjectNotifier notifier;

    @Nullable private final OpNode rootOpNode;

    private Options lastOptions;

    private final List<TreeTableColumnDefinition<OpNode, ?>> columnDefinitions = new ArrayList<>();

    public TraceTreeViewPanel(Project project, @Nullable OpNode rootOpNode) {
        super(new BorderLayout());

        this.rootOpNode = rootOpNode;

        MessageBus bus = project.getMessageBus();
        this.notifier = bus.syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC);

        initLayout();

        TraceManager tm = TraceManager.getInstance(project);
        applyOptions(tm.getOptions());
    }

    private void initLayout() {
        add(initMain());
    }

    private JComponent initMain() {

        for (TraceTreeViewColumn column : TraceTreeViewColumn.values()) {
            columnDefinitions.add(new TreeTableColumnDefinition<>(column));
        }

        traceTreeTableModel = new TraceTreeTableModel(columnDefinitions, rootOpNode);

        main = MidPointUtils.createTable2(traceTreeTableModel, MidPointUtils.createTableColumnModel(columnDefinitions));
        main.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        main.addTreeSelectionListener(this::mainTableSelectionChanged);

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

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> main.collapseAll());
        group.add(collapseAll);

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("TraceViewPanelMainToolbar", group, true);
        return resultsActionsToolbar.getComponent();
    }

    private void mainTableSelectionChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        selectedTraceNodeChange(path);
    }

    public void selectedTraceNodeChange(TreePath path) {
        if (path != null) {
            AbstractTraceTreeTableNode node = (AbstractTraceTreeTableNode) path.getLastPathComponent();
            notifier.selectedTraceNodeChange(node != null ? node.getUserObject() : null);
        } else {
            notifier.selectedTraceNodeChange(null);
        }
    }

    public void selectNotify() {
        int index = main.getSelectionModel().getLeadSelectionIndex();
        if (index >= 0) {
            TreePath path = main.getPathForRow(index);
            selectedTraceNodeChange(path);
        } else {
            notifier.selectedTraceNodeChange(null);
        }
    }

    public void deselectNotify() {
        selectedTraceNodeChange(null);
    }

    public void applyOptions(Options options) {
        LOG.debug("Applying options", options);

        if (lastOptions == null || lastOptions.nodeVisibilityDiffers(options)) {
            if (rootOpNode != null) {
                options.applyVisibilityTo(rootOpNode);
            }
            traceTreeTableModel.updateParentChildLinks();
            traceTreeTableModel.fireChange();
        }

        DefaultTableColumnModelExt realColumnModel = (DefaultTableColumnModelExt) main.getColumnModel();
        List<TableColumn> realColumns = realColumnModel.getColumns(true);
        for (int i = 0; i < columnDefinitions.size(); i++) {
            TreeTableColumnDefinition<OpNode, ?> columnDef = columnDefinitions.get(i);
            Object identifier = realColumns.get(i).getIdentifier();
            TableColumnExt realColumnExt = realColumnModel.getColumnExt(identifier);

            //noinspection SuspiciousMethodCalls
            boolean visible = options.getColumnsToShow().contains(columnDef.getOriginalColumnDefinition());
            columnDef.setVisible(visible);
            realColumnExt.setVisible(visible);

            System.out.println("Set visible = " + visible + " for " + identifier + " (" + realColumnExt + ")");
        }

        lastOptions = options.clone();
    }

}
