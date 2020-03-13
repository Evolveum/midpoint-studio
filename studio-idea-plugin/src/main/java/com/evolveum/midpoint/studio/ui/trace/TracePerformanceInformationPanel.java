package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.PerformanceCategory;
import com.evolveum.midpoint.studio.impl.trace.PerformanceCategoryInfo;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SingleOperationPerformanceInformationType;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.evolveum.midpoint.studio.util.MidPointUtils.formatTime;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TracePerformanceInformationPanel extends BorderLayoutPanel {

    private JBTable category;

    private JBTable operation;

    public TracePerformanceInformationPanel() {
        initLayout();
    }

    private void initLayout() {
        JBSplitter split = new OnePixelSplitter(false);

        List<TreeTableColumnDefinition<Map.Entry<PerformanceCategory, PerformanceCategoryInfo>, ?>> categoryColumns = new ArrayList<>();
        categoryColumns.add(new TreeTableColumnDefinition<>("Category", 200, o -> o.getKey().getLabel()));
        categoryColumns.add(new TreeTableColumnDefinition<>("Total #", 50, o -> o.getValue().getTotalCount()));
        categoryColumns.add(new TreeTableColumnDefinition<>("Total time", 70, o -> o.getValue().getTotalTime()));
        categoryColumns.add(new TreeTableColumnDefinition<>("Own #", 50, o -> o.getValue().getOwnCount()));
        categoryColumns.add(new TreeTableColumnDefinition<>("Own time", 70, o -> o.getValue().getOwnTime()));

        this.category = new JBTable(new ListTableModel(categoryColumns, new ArrayList<>()));  // todo data
        split.setFirstComponent(new JBScrollPane(category));

        List<TreeTableColumnDefinition<SingleOperationPerformanceInformationType, ?>> operationColumns = new ArrayList<>();
        operationColumns.add(new TreeTableColumnDefinition<>("Operation", 500, o -> o.getName()));
        operationColumns.add(new TreeTableColumnDefinition<>("Count", 50, o -> o.getInvocationCount()));
        operationColumns.add(new TreeTableColumnDefinition<>("Total time", 100, o -> o.getTotalTime()));
        operationColumns.add(new TreeTableColumnDefinition<>("Min", 50, o -> o.getMinTime()));
        operationColumns.add(new TreeTableColumnDefinition<>("Max", 50, o -> o.getMaxTime()));
        operationColumns.add(new TreeTableColumnDefinition<>("Avg", 50, o -> formatTime(o.getTotalTime() / o.getInvocationCount())));

        this.operation = new JBTable(new ListTableModel<>(operationColumns, new ArrayList<>()));    // todo data
        split.setSecondComponent(new JBScrollPane(operation));

        add(split, BorderLayout.CENTER);
    }
}
