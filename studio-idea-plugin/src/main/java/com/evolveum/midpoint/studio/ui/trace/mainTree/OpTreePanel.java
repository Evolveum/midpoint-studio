package com.evolveum.midpoint.studio.ui.trace.mainTree;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.OpType;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.trace.Options;
import com.evolveum.midpoint.studio.impl.trace.TraceService;
import com.evolveum.midpoint.studio.ui.trace.lens.TraceTreeViewColumn;
import com.evolveum.midpoint.studio.ui.trace.mainTree.model.AbstractOpTreeTableNode;
import com.evolveum.midpoint.studio.ui.trace.mainTree.model.OpTreeTableModel;
import com.evolveum.midpoint.studio.ui.trace.options.PredefinedColumnSet;
import com.evolveum.midpoint.studio.ui.trace.options.PredefinedOpTypeSet;
import com.evolveum.midpoint.studio.ui.trace.options.PredefinedPerformanceCategoriesSet;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel holding tree of OpNodes. This is the main view of the trace viewer.
 */
public class OpTreePanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(OpTreePanel.class);

    private final Project project;

    private DefaultTreeTable<OpNode, OpTreeTableModel> traceTreeTable;

    private OpTreeTableModel opTreeTableModel;

    private final MidPointProjectNotifier notifier;

    @Nullable
    private final OpNode rootOpNode;

    private Options lastOptions;

    /** All columns in definition order, keyed by their TraceTreeViewColumn enum value. */
    private final Map<TraceTreeViewColumn, TableColumn> allColumns = new LinkedHashMap<>();

    public OpTreePanel(Project project, @Nullable OpNode rootOpNode) {
        super(new BorderLayout());

        this.project = project;
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
        List<DefaultColumnInfo<OpNode, ?>> columnInfos = Arrays.stream(TraceTreeViewColumn.values())
                .map(TraceTreeViewColumn::toColumnInfo)
                .collect(Collectors.toList());

        opTreeTableModel = new OpTreeTableModel(columnInfos, rootOpNode);
        traceTreeTable = new DefaultTreeTable<>(opTreeTableModel);

        traceTreeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        traceTreeTable.getTree().setRootVisible(false);
        traceTreeTable.getTree().addTreeSelectionListener(this::traceTreeTableSelectionChanged);

        // Capture all TableColumn objects keyed by the TraceTreeViewColumn enum value
        TraceTreeViewColumn[] cols = TraceTreeViewColumn.values();
        for (int i = 0; i < cols.length; i++) {
            TableColumn tc = traceTreeTable.getColumnModel().getColumn(i);
            tc.setIdentifier(cols[i]);
            allColumns.put(cols[i], tc);
        }

        JPanel panel = new BorderLayoutPanel();
        panel.add(initMainToolbar(), BorderLayout.NORTH);
        panel.add(new JBScrollPane(traceTreeTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        return panel;
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> expandAll()));
        group.add(MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> collapseAll()));
        group.add(MidPointUtils.createAnAction("Show direct children", AllIcons.General.Add, e -> setChildrenVisible(false)));
        group.add(MidPointUtils.createAnAction("Show all children", AllIcons.Actions.ShowAsTree, e -> setChildrenVisible(true)));
        group.add(MidPointUtils.createAnAction("Hide selected", AllIcons.General.HideToolWindow, e -> hideSelected(false)));
        group.add(MidPointUtils.createAnAction("Hide selected (tree)", AllIcons.Actions.DeleteTagHover, e -> hideSelected(true)));
        group.addSeparator();
        group.add(createOpTypeFilterAction());
        group.add(createCategoryFilterAction());
        group.add(createColumnFilterAction());

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("TraceViewPanelMainToolbar", group, true);
        toolbar.setTargetComponent(this);

        return toolbar.getComponent();
    }

    private DefaultActionGroup createOpTypeFilterAction() {
        DefaultActionGroup filterGroup = new DefaultActionGroup("Filter operation types", true);
        filterGroup.getTemplatePresentation().setIcon(AllIcons.General.Filter);

        for (PredefinedOpTypeSet set : PredefinedOpTypeSet.values()) {
            filterGroup.add(new ToggleAction(set.toString()) {

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }

                @Override
                public boolean isSelected(@NotNull AnActionEvent e) {
                    Collection<OpType> setTypes = set.getTypes();
                    java.util.Set<OpType> typesToShow = TraceService.getInstance(project).getOptions().getTypesToShow();
                    return typesToShow.size() == setTypes.size() && typesToShow.containsAll(setTypes);
                }

                @Override
                public void setSelected(@NotNull AnActionEvent e, boolean state) {
                    Options options = TraceService.getInstance(project).getOptions().clone();
                    if (state) {
                        options.getTypesToShow().clear();
                        options.getTypesToShow().addAll(set.getTypes());
                    } else {
                        options.getTypesToShow().removeAll(set.getTypes());
                    }
                    TraceService.getInstance(project).setOptions(options);
                }
            });
        }

        filterGroup.addSeparator();

        for (OpType type : OpType.values()) {
            filterGroup.add(new ToggleAction(type.getLabel()) {

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }

                @Override
                public boolean isSelected(@NotNull AnActionEvent e) {
                    return TraceService.getInstance(project).getOptions().getTypesToShow().contains(type);
                }

                @Override
                public void setSelected(@NotNull AnActionEvent e, boolean state) {
                    Options options = TraceService.getInstance(project).getOptions().clone();
                    if (state) {
                        options.getTypesToShow().add(type);
                    } else {
                        options.getTypesToShow().remove(type);
                    }
                    TraceService.getInstance(project).setOptions(options);
                }
            });
        }

        return filterGroup;
    }

    private DefaultActionGroup createCategoryFilterAction() {
        DefaultActionGroup filterGroup = new DefaultActionGroup("Filter categories", true);
        filterGroup.getTemplatePresentation().setIcon(AllIcons.Actions.GroupBy);

        for (PredefinedPerformanceCategoriesSet set : PredefinedPerformanceCategoriesSet.values()) {
            filterGroup.add(new ToggleAction(set.toString()) {

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }

                @Override
                public boolean isSelected(@NotNull AnActionEvent e) {
                    Options opts = TraceService.getInstance(project).getOptions();
                    Collection<PerformanceCategory> setCategories = set.getCategories();
                    java.util.Set<PerformanceCategory> categoriesToShow = opts.getCategoriesToShow();
                    return categoriesToShow.size() == setCategories.size()
                            && categoriesToShow.containsAll(setCategories)
                            && opts.isShowAlsoParents() == set.isShowParents();
                }

                @Override
                public void setSelected(@NotNull AnActionEvent e, boolean state) {
                    Options options = TraceService.getInstance(project).getOptions().clone();
                    if (state) {
                        options.getCategoriesToShow().clear();
                        options.getCategoriesToShow().addAll(set.getCategories());
                        options.setShowAlsoParents(set.isShowParents());
                    } else {
                        options.getCategoriesToShow().removeAll(set.getCategories());
                    }
                    TraceService.getInstance(project).setOptions(options);
                }
            });
        }

        filterGroup.addSeparator();

        filterGroup.add(new ToggleAction("Show also parents") {

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return TraceService.getInstance(project).getOptions().isShowAlsoParents();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                Options options = TraceService.getInstance(project).getOptions().clone();
                options.setShowAlsoParents(state);
                TraceService.getInstance(project).setOptions(options);
            }
        });

        filterGroup.addSeparator();

        for (PerformanceCategory category : PerformanceCategory.values()) {
            filterGroup.add(new ToggleAction(category.getLabel()) {

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }

                @Override
                public boolean isSelected(@NotNull AnActionEvent e) {
                    return TraceService.getInstance(project).getOptions().getCategoriesToShow().contains(category);
                }

                @Override
                public void setSelected(@NotNull AnActionEvent e, boolean state) {
                    Options options = TraceService.getInstance(project).getOptions().clone();
                    if (state) {
                        options.getCategoriesToShow().add(category);
                    } else {
                        options.getCategoriesToShow().remove(category);
                    }
                    TraceService.getInstance(project).setOptions(options);
                }
            });
        }

        return filterGroup;
    }

    private DefaultActionGroup createColumnFilterAction() {
        DefaultActionGroup filterGroup = new DefaultActionGroup("Filter columns", true);
        filterGroup.getTemplatePresentation().setIcon(AllIcons.Actions.SplitVertically);

        for (PredefinedColumnSet set : PredefinedColumnSet.values()) {
            filterGroup.add(new ToggleAction(set.toString()) {

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }

                @Override
                public boolean isSelected(@NotNull AnActionEvent e) {
                    Collection<TraceTreeViewColumn> setColumns = set.getColumns();
                    java.util.Set<TraceTreeViewColumn> columnsToShow = TraceService.getInstance(project).getOptions().getColumnsToShow();
                    return columnsToShow.size() == setColumns.size() && columnsToShow.containsAll(setColumns);
                }

                @Override
                public void setSelected(@NotNull AnActionEvent e, boolean state) {
                    Options options = TraceService.getInstance(project).getOptions().clone();
                    if (state) {
                        options.getColumnsToShow().clear();
                        options.getColumnsToShow().addAll(set.getColumns());
                    } else {
                        options.getColumnsToShow().removeAll(set.getColumns());
                    }
                    TraceService.getInstance(project).setOptions(options);
                }
            });
        }

        filterGroup.addSeparator();

        for (TraceTreeViewColumn column : TraceTreeViewColumn.values()) {
            filterGroup.add(new ToggleAction(column.getName()) {

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }

                @Override
                public boolean isSelected(@NotNull AnActionEvent e) {
                    return TraceService.getInstance(project).getOptions().getColumnsToShow().contains(column);
                }

                @Override
                public void setSelected(@NotNull AnActionEvent e, boolean state) {
                    Options options = TraceService.getInstance(project).getOptions().clone();
                    if (state) {
                        options.getColumnsToShow().add(column);
                    } else {
                        options.getColumnsToShow().remove(column);
                    }
                    TraceService.getInstance(project).setOptions(options);
                }
            });
        }

        return filterGroup;
    }

    private void expandAll() {
        for (int i = 0; i < traceTreeTable.getRowCount(); i++) {
            traceTreeTable.getTree().expandRow(i);
        }
    }

    private void collapseAll() {
        for (int i = traceTreeTable.getRowCount() - 1; i >= 0; i--) {
            traceTreeTable.getTree().collapseRow(i);
        }
    }

    private void hideSelected(boolean deep) {
        List<TreePath> selectedPaths = getSelectedPaths();
        for (TreePath selectedPath : selectedPaths) {
            AbstractOpTreeTableNode node = (AbstractOpTreeTableNode) selectedPath.getLastPathComponent();
            OpNode opNode = node.getUserObject();
            assert opNode != null;
            setNodeVisible(opNode, false, deep);
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
            AbstractOpTreeTableNode node = (AbstractOpTreeTableNode) selectedPath.getLastPathComponent();
            OpNode opNode = node.getUserObject();
            assert opNode != null;
            for (OpNode child : opNode.getChildren()) {
                setNodeVisible(child, true, deep);
            }
        }
        updateLinksAndRefresh(selectedPaths);
    }

    private void updateLinksAndRefresh(Collection<TreePath> changedPaths) {
        opTreeTableModel.updateParentChildLinks();
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
            selectedPaths.add(traceTreeTable.getTree().getPathForRow(selectedRow));
        }
        return selectedPaths;
    }

    private void traceTreeTableSelectionChanged(TreeSelectionEvent e) {
        selectedTraceNodeChange(e.getNewLeadSelectionPath());
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
            TreePath path = traceTreeTable.getTree().getPathForRow(index);
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

        applyColumnVisibility(options);

        lastOptions = options.clone();
    }

    private void applyColumnVisibility(Options options) {
        Set<TraceTreeViewColumn> visibleCols = options.getColumnsToShow();

        // Remove all tracked columns from the table's column model
        for (TableColumn tc : allColumns.values()) {
            traceTreeTable.removeColumn(tc);
        }

        // Re-add only the visible ones in original definition order
        for (Map.Entry<TraceTreeViewColumn, TableColumn> entry : allColumns.entrySet()) {
            if (visibleCols.contains(entry.getKey())) {
                traceTreeTable.addColumn(entry.getValue());
            }
        }
    }
}
