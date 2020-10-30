package com.evolveum.midpoint.studio.ui.performance.options;

import com.evolveum.midpoint.studio.impl.performance.PerformanceOptions;
import com.evolveum.midpoint.studio.impl.trace.TraceService;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.evolveum.midpoint.studio.ui.performance.mainTree.PerformanceTreeViewColumn;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
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
 *
 */
public class OptionsPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(OptionsPanel.class);

    private TraceService traceManager;

    /**
     * Predefined set of columns.
     */
    ComboBox<PredefinedColumnSet> predefinedColumnsBox;

    /**
     * Check boxes for individual columns (Operation, Invocations, Inv/sample, ...)
     */
    private final Map<PerformanceTreeViewColumn, JCheckBox> columnsChecks = new HashMap<>();

    public OptionsPanel(Project project) {
        this.traceManager = TraceService.getInstance(project);
        initLayout();
    }

    private void initLayout() {
        createToolbar();

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setAlignmentX(Component.LEFT_ALIGNMENT);

        createColumnsPanel(root);

        add(MidPointUtils.borderlessScrollPane(root));

        //viewTypeChanged(viewTypeComboboxAction.getOpView());
    }

    private void createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        AnAction apply = new AnAction("Apply", "Apply options changes", AllIcons.Actions.Commit) {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                applyPerformed();
            }
        };
        group.add(apply);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("PerformanceOptionsToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);
    }

    private void createColumnsPanel(JPanel root) {
        JPanel columnsPanel = createBoxLayoutPanel();
        columnsPanel.setBorder(JBUI.Borders.empty(5));

        predefinedColumnsBox = new ComboBox<>(PredefinedColumnSet.values());
        predefinedColumnsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        predefinedColumnsBox.addActionListener(e -> {
            PredefinedColumnSet predefinedSet = (PredefinedColumnSet) predefinedColumnsBox.getSelectedItem();
            if (predefinedSet != null) {
                columnsChecks.forEach((column, checkBox) -> checkBox.setSelected(predefinedSet.contains(column)));
            }
        });
        columnsPanel.add(predefinedColumnsBox);

        for (PerformanceTreeViewColumn column : PerformanceTreeViewColumn.values()) {
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

//    private void viewTypeChanged(PredefinedOpView predefinedOpView) {
//        LOG.debug("View type changed to: ", predefinedOpView);
//
//        traceManager.setOpViewType(predefinedOpView);
//        predefinedOpTypesBox.setSelectedItem(predefinedOpView.getOpTypeSet());
//        predefinedCategoriesBox.setSelectedItem(predefinedOpView.getCategoriesSet());
//        predefinedColumnsBox.setSelectedItem(predefinedOpView.getColumnSet());
//
//        invalidate();
//    }

    private void applyPerformed() {
        PerformanceOptions options = createOptions();
        traceManager.setPerformanceOptions(options);
    }

    private PerformanceOptions createOptions() {
        PerformanceOptions rv = new PerformanceOptions();
        columnsChecks.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .forEach(column -> rv.getColumnsToShow().add(column));

        return rv;
    }
}
