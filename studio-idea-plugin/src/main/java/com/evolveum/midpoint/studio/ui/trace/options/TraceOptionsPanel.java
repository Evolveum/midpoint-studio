package com.evolveum.midpoint.studio.ui.trace.options;

import com.evolveum.midpoint.schema.traces.OpType;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.impl.trace.Options;
import com.evolveum.midpoint.studio.impl.trace.TraceService;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.evolveum.midpoint.studio.ui.trace.mainTree.TraceTreeViewColumn;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceOptionsPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(TraceOptionsPanel.class);

    private TraceService traceManager;

    /**
     * Predefined view e.g. "All", "Functional overview", ...
     */
    private ViewTypeComboboxAction viewTypeComboboxAction;

    /**
     * Predefined set of operation types.
     */
    ComboBox<PredefinedOpTypeSet> predefinedOpTypesBox;

    /**
     * Check boxes for operation (node) types, e.g. "Clockwork run", "Clockwork click", ...
     */
    private final Map<OpType, JCheckBox> opTypesChecks = new HashMap<>();

    /**
     * Predefined set of categories.
     */
    ComboBox<PredefinedPerformanceCategoriesSet> predefinedCategoriesBox;

    /**
     * Check boxes for performance categories, e.g. Repo, Repo:R, Repo:W, ICF, ...
     */
    private final Map<PerformanceCategory, JCheckBox> categoriesChecks = new HashMap<>();

    private JCheckBox alsoParentsCheck;

    /**
     * Predefined set of columns.
     */
    ComboBox<PredefinedColumnSet> predefinedColumnsBox;

    /**
     * Check boxes for individual columns (Operation, State, EW, ...)
     */
    private final Map<TraceTreeViewColumn, JCheckBox> columnsChecks = new HashMap<>();

    public TraceOptionsPanel(Project project) {
        this.traceManager = TraceService.getInstance(project);

        initLayout();
    }

    private void initLayout() {
        createToolbar();

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setAlignmentX(Component.LEFT_ALIGNMENT);

        createOpTypesPanel(root);
        createCategoriesPanel(root);
        createColumnsPanel(root);

        add(MidPointUtils.borderlessScrollPane(root));

        viewTypeChanged(viewTypeComboboxAction.getOpView());
    }

    private void createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        viewTypeComboboxAction = new ViewTypeComboboxAction(traceManager.getOpViewType()) {

            @Override
            public void setOpView(PredefinedOpView opView) {
                super.setOpView(opView);

                viewTypeChanged(opView);
            }
        };
        group.add(viewTypeComboboxAction);

        AnAction apply = new AnAction("Apply", "Apply options changes", AllIcons.Actions.Commit) {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                applyPerformed();
            }
        };
        group.add(apply);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceOptionsToolbar", group, true);
        toolbar.setTargetComponent(this);
        add(toolbar.getComponent(), BorderLayout.NORTH);
    }

    private void createOpTypesPanel(JPanel root) {
        JPanel opTypesPanel = createBoxLayoutPanel();
        opTypesPanel.setBorder(JBUI.Borders.empty(5));

        predefinedOpTypesBox = new ComboBox<>(PredefinedOpTypeSet.values());
        predefinedOpTypesBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        predefinedOpTypesBox.addActionListener(e -> {
            PredefinedOpTypeSet predefinedSet = (PredefinedOpTypeSet) predefinedOpTypesBox.getSelectedItem();
            LOG.info("Buhahaha " + predefinedSet);
            if (predefinedSet != null) {
                opTypesChecks.forEach((opType, checkBox) -> checkBox.setSelected(predefinedSet.contains(opType)));
            }
        });
        opTypesPanel.add(predefinedOpTypesBox);

        for (OpType type : OpType.values()) {
            JCheckBox check = new JCheckBox();
            check.setText(type.getLabel());
            opTypesPanel.add(check);

            opTypesChecks.put(type, check);
        }

        root.add(new HeaderDecorator("Operation types to show", opTypesPanel), BorderLayout.NORTH);
    }

    private void createCategoriesPanel(JPanel root) {
        JPanel categories = createBoxLayoutPanel();
        categories.setBorder(JBUI.Borders.empty(5));

        predefinedCategoriesBox = new ComboBox<>(PredefinedPerformanceCategoriesSet.values());
        predefinedCategoriesBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        predefinedCategoriesBox.addActionListener(e -> {
            PredefinedPerformanceCategoriesSet predefinedSet = (PredefinedPerformanceCategoriesSet) predefinedCategoriesBox.getSelectedItem();
            LOG.info("Buhahaha " + predefinedSet);
            if (predefinedSet != null) {
                categoriesChecks.forEach((category, checkBox) -> checkBox.setSelected(predefinedSet.contains(category)));
                alsoParentsCheck.setSelected(predefinedSet.isShowParents());
            }
        });
        categories.add(predefinedCategoriesBox);

        for (PerformanceCategory type : PerformanceCategory.values()) {
            JCheckBox check = new JCheckBox();
            check.setText(type.getLabel());
            categories.add(check);

            categoriesChecks.put(type, check);
        }

        alsoParentsCheck = new JCheckBox();
        alsoParentsCheck.setText("Show also parents");
        categories.add(alsoParentsCheck);

        root.add(new HeaderDecorator("Categories to show", categories), BorderLayout.CENTER);
    }

    private void createColumnsPanel(JPanel root) {
        JPanel columnsPanel = createBoxLayoutPanel();
        columnsPanel.setBorder(JBUI.Borders.empty(5));

        predefinedColumnsBox = new ComboBox<>(PredefinedColumnSet.values());
        predefinedColumnsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        predefinedColumnsBox.addActionListener(e -> {
            PredefinedColumnSet predefinedSet = (PredefinedColumnSet) predefinedColumnsBox.getSelectedItem();
            LOG.info("Buhahaha " + predefinedSet);
            if (predefinedSet != null) {
                columnsChecks.forEach((column, checkBox) -> checkBox.setSelected(predefinedSet.contains(column)));
            }
        });
        columnsPanel.add(predefinedColumnsBox);

        for (TraceTreeViewColumn column : TraceTreeViewColumn.values()) {
            JCheckBox check = new JCheckBox();
            check.setText(column.getLabel());
            check.setSelected(true);

            columnsPanel.add(check);
            columnsChecks.put(column, check);
        }

        root.add(new HeaderDecorator("Columns to show", columnsPanel), BorderLayout.SOUTH);
    }

    private JPanel createBoxLayoutPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(JBUI.Borders.emptyLeft(5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    private void viewTypeChanged(PredefinedOpView predefinedOpView) {
        LOG.debug("View type changed to: ", predefinedOpView);

        traceManager.setOpViewType(predefinedOpView);
        predefinedOpTypesBox.setSelectedItem(predefinedOpView.getOpTypeSet());
        predefinedCategoriesBox.setSelectedItem(predefinedOpView.getCategoriesSet());
        predefinedColumnsBox.setSelectedItem(predefinedOpView.getColumnSet());

        invalidate();
    }

    private void applyPerformed() {
        Options options = createOptions();
        traceManager.setOptions(options);
    }

    private Options createOptions() {
        Options rv = new Options();
        for (Map.Entry<OpType, JCheckBox> e : opTypesChecks.entrySet()) {
            if (e.getValue().isSelected()) {
                rv.getTypesToShow().add(e.getKey());
            }
        }
        for (Map.Entry<PerformanceCategory, JCheckBox> e : categoriesChecks.entrySet()) {
            if (e.getValue().isSelected()) {
                rv.getCategoriesToShow().add(e.getKey());
            }
        }
        rv.setShowAlsoParents(alsoParentsCheck.isSelected());
        columnsChecks.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .forEach(column -> rv.getColumnsToShow().add(column));

        return rv;
    }

    private static class ViewTypeComboboxAction extends ComboBoxAction {

        private PredefinedOpView opView;

        public ViewTypeComboboxAction(PredefinedOpView opView) {
            this.opView = opView;
        }

        @NotNull
        @Override
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            DefaultActionGroup group = new DefaultActionGroup();
            for (PredefinedOpView o : PredefinedOpView.values()) {
                group.add(new ViewTypeAction(o) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        ViewTypeComboboxAction.this.setOpView(this.getOpView());
                    }
                });
            }

            return group;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);

            PredefinedOpView o = getOpView();
            if (o == null) {
                return;
            }

            String text = o.getLabel();
            getTemplatePresentation().setText(text);
            e.getPresentation().setText(text);
        }

        public void setOpView(PredefinedOpView opView) {
            this.opView = opView;
        }

        public PredefinedOpView getOpView() {
            return opView;
        }
    }

    private static abstract class ViewTypeAction extends AnAction implements DumbAware {

        private final PredefinedOpView opView;

        public ViewTypeAction(PredefinedOpView opView) {
            super(opView != null ? opView.getLabel() : "");
            this.opView = opView;
        }

        public PredefinedOpView getOpView() {
            return opView;
        }
    }
}
