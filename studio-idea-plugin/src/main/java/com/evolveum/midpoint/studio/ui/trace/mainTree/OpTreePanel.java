package com.evolveum.midpoint.studio.ui.trace.mainTree;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.MidPointIcons;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.trace.Options;
import com.evolveum.midpoint.studio.impl.trace.TraceService;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.ui.trace.lens.TraceTreeViewColumn;
import com.evolveum.midpoint.studio.ui.trace.mainTree.model.AbstractOpTreeTableNode;
import com.evolveum.midpoint.studio.ui.trace.mainTree.model.OpTreeTableModel;
import com.evolveum.midpoint.studio.ui.trace.presentation.AbstractOpNodePresentation;
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
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel holding tree of OpNodes. This is the main view of the trace viewer.
 */
public class OpTreePanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(OpTreePanel.class);

    private JXTreeTable traceTreeTable;

    private OpTreeTableModel opTreeTableModel;

    private final MidPointProjectNotifier notifier;

    @Nullable
    private final OpNode rootOpNode;

    private Options lastOptions;

    private final List<TreeTableColumnDefinition<OpNode, ?>> columnDefinitions = new ArrayList<>();

    public OpTreePanel(Project project, @Nullable OpNode rootOpNode) {
        super(new BorderLayout());

        this.rootOpNode = rootOpNode;

        MessageBus bus = project.getMessageBus();
        this.notifier = bus.syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC);

        initLayout();

        TraceService tm = TraceService.getInstance(project);
        applyOptions(tm.getOptions());
    }

    private void initLayout() {
        add(initMain());
    }

    private JComponent initMain() {

        for (TraceTreeViewColumn column : TraceTreeViewColumn.values()) {
            columnDefinitions.add(new TreeTableColumnDefinition<>(column));
        }

        opTreeTableModel = new OpTreeTableModel(columnDefinitions, rootOpNode);

        traceTreeTable = MidPointUtils.createTable2(opTreeTableModel, MidPointUtils.createTableColumnModel(columnDefinitions), false);

        traceTreeTable.addHighlighter(new AbstractHighlighter() {
            @Override
            protected Component doHighlight(Component component, ComponentAdapter adapter) {
                int row = adapter.convertRowIndexToModel(adapter.row);
                TreePath pathForRow = traceTreeTable.getPathForRow(row);
                AbstractOpTreeTableNode node = (AbstractOpTreeTableNode) pathForRow.getLastPathComponent();
                OpNode opNode = node.getUserObject();
                if (opNode != null) {
                    AbstractOpNodePresentation<?> presentation = (AbstractOpNodePresentation<?>) opNode.getPresentation(); // fixme hack
                    if (adapter.isSelected()) {
                        component.setBackground(traceTreeTable.getSelectionBackground());
                    } else {
                        Color backgroundColor = presentation.getBackgroundColor();
                        if (backgroundColor != null) {
                            component.setBackground(backgroundColor);
                        } else {
                            component.setBackground(traceTreeTable.getBackground());
                        }
                    }
                }
                return component;
            }
        });


        traceTreeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        traceTreeTable.addTreeSelectionListener(this::traceTreeTableSelectionChanged);

        JPanel panel = new BorderLayoutPanel();
        JComponent toolbar = initMainToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        panel.add(new JBScrollPane(traceTreeTable), BorderLayout.CENTER);

        return panel;
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> traceTreeTable.expandAll()));
        group.add(MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> traceTreeTable.collapseAll()));
        group.add(MidPointUtils.createAnAction("Show direct children", AllIcons.General.Add, e -> setChildrenVisible(false)));
        group.add(MidPointUtils.createAnAction("Show all children", AllIcons.Actions.ShowAsTree, e -> setChildrenVisible(true)));
        group.add(MidPointUtils.createAnAction("Hide selected", AllIcons.General.HideToolWindow, e -> hideSelected(false)));
        group.add(MidPointUtils.createAnAction("Hide selected (tree)", AllIcons.Actions.DeleteTagHover, e -> hideSelected(true)));

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("TraceViewPanelMainToolbar", group, true);
        resultsActionsToolbar.setTargetComponent(this);
        return resultsActionsToolbar.getComponent();
    }

    private void hideSelected(boolean deep) {
        List<TreePath> selectedPaths = getSelectedPaths();
        for (TreePath selectedPath : selectedPaths) {
            AbstractOpTreeTableNode selectedTreeNode = (AbstractOpTreeTableNode) selectedPath.getLastPathComponent();
            OpNode selectedOpNode = selectedTreeNode.getUserObject();
            assert selectedOpNode != null;
            setNodeVisible(selectedOpNode, false, deep);
        }
        updateLinksAndRefresh(getParentPaths(selectedPaths));
    }

    @NotNull
    private Collection<TreePath> getParentPaths(List<TreePath> selectedPaths) {
        return selectedPaths.stream()
                .map(TreePath::getParentPath)
                .collect(Collectors.toSet());
    }

    private void setChildrenVisible(boolean deep) {
        List<TreePath> selectedPaths = getSelectedPaths();
        for (TreePath selectedPath : selectedPaths) {
            AbstractOpTreeTableNode selectedTreeNode = (AbstractOpTreeTableNode) selectedPath.getLastPathComponent();
            OpNode selectedOpNode = selectedTreeNode.getUserObject();
            assert selectedOpNode != null;
            for (OpNode child : selectedOpNode.getChildren()) {
                setNodeVisible(child, true, deep);
            }
        }
        updateLinksAndRefresh(selectedPaths);
    }

    private void updateLinksAndRefresh(Collection<TreePath> changedPaths) {
        opTreeTableModel.updateParentChildLinks(); // todo restrict in scope
        for (TreePath changedPath : changedPaths) {
            opTreeTableModel.firePathChanged(changedPath);
        }
    }

    private void setNodeVisible(OpNode node, boolean value, boolean deep) {
        node.setVisible(value);
        if (deep) {
            node.getChildren().forEach(child -> setNodeVisible(child, value, true));
        }
    }

    @NotNull
    private List<TreePath> getSelectedPaths() {
        List<TreePath> selectedPaths = new ArrayList<>();
        int[] selectedRows = traceTreeTable.getSelectedRows();
        for (int selectedRow : selectedRows) {
            selectedPaths.add(traceTreeTable.getPathForRow(selectedRow));
        }
        return selectedPaths;
    }

    private void traceTreeTableSelectionChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        selectedTraceNodeChange(path);
    }

    public void selectedTraceNodeChange(TreePath path) {
        if (path != null) {
            AbstractOpTreeTableNode node = (AbstractOpTreeTableNode) path.getLastPathComponent();
            notifier.selectedTraceNodeChange(node != null ? node.getUserObject() : null);
        } else {
            notifier.selectedTraceNodeChange(null);
        }
    }

    public void selectNotify() {
        int index = traceTreeTable.getSelectionModel().getLeadSelectionIndex();
        if (index >= 0) {
            TreePath path = traceTreeTable.getPathForRow(index);
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
            opTreeTableModel.updateParentChildLinks();
            opTreeTableModel.fireChange();
        }

        DefaultTableColumnModelExt realColumnModel = (DefaultTableColumnModelExt) traceTreeTable.getColumnModel();
        List<TableColumn> realColumns = realColumnModel.getColumns(true);
        for (int i = 0; i < columnDefinitions.size(); i++) {
            TreeTableColumnDefinition<OpNode, ?> columnDef = columnDefinitions.get(i);
            Object identifier = realColumns.get(i).getIdentifier();
            TableColumnExt realColumnExt = realColumnModel.getColumnExt(identifier);

            //noinspection SuspiciousMethodCalls
            boolean visible = options.getColumnsToShow().contains(columnDef.getOriginalColumnDefinition());
            columnDef.setVisible(visible);
            realColumnExt.setVisible(visible);

//            System.out.println("Set visible = " + visible + " for " + identifier + " (" + realColumnExt + ")");
        }

        lastOptions = options.clone();
    }

}
