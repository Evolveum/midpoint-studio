package com.evolveum.midpoint.studio.ui.performance.singleOp;

import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.schema.traces.PerformanceCategoryInfo;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.ui.trace.singleOp.model.ListTableModel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SingleOperationPerformanceInformationType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SingleOpPerformancePanel extends BorderLayoutPanel {

    ListTableModel<Map.Entry<PerformanceCategory, PerformanceCategoryInfo>> categoryModel;

    ListTableModel<SingleOperationPerformanceInformationType> operationModel;

    int samples;

    public SingleOpPerformancePanel(Project project) {
        initLayout();

        project.getMessageBus().connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifier() {

            @Override
            public void selectedPerformanceNodeChange(OperationPerformance node) {
                System.out.println("SingleOpPerformancePanel: new node = " + node);
                nodeChange(node);
            }
        });
    }

    private void initLayout() {
        JBSplitter split = new OnePixelSplitter(false);

        List<TreeTableColumnDefinition<Map.Entry<PerformanceCategory, PerformanceCategoryInfo>, ?>> categoryColumns = new ArrayList<>();
        categoryColumns.add(new TreeTableColumnDefinition<>("Category", 200, o -> o.getKey().getLabel()));
        categoryColumns.add(new TreeTableColumnDefinition<>("Invocations", 50, o -> getInvocationsPerSample(o.getValue())));
        categoryColumns.add(new TreeTableColumnDefinition<>("Time", 70, o -> getTimePerSample(o.getValue())));

        categoryModel = new ListTableModel<>(categoryColumns, new ArrayList<>());
        JBTable category = new JBTable(categoryModel);

        List<TreeTableColumnDefinition<SingleOperationPerformanceInformationType, ?>> operationColumns = new ArrayList<>();
        operationColumns.add(new TreeTableColumnDefinition<>("Operation", 500, SingleOperationPerformanceInformationType::getName));
        operationColumns.add(new TreeTableColumnDefinition<>("Invocations", 50, this::getInvocationsPerSample));
        operationColumns.add(new TreeTableColumnDefinition<>("Total time", 100, this::getTimePerSample));
        operationColumns.add(new TreeTableColumnDefinition<>("Time/invocation", 50, this::getTimePerInvocation));
        operationModel = new ListTableModel<>(operationColumns, new ArrayList<>());
        JBTable operation = new JBTable(operationModel);


        split.setFirstComponent(new JBScrollPane(category));
        split.setSecondComponent(new JBScrollPane(operation));
        add(split, BorderLayout.CENTER);
    }

    private void nodeChange(OperationPerformance node) {
        if (node != null) {
            samples = node.getTree().getSamples();
            List<Map.Entry<PerformanceCategory, PerformanceCategoryInfo>> categories =
                    new ArrayList<>(node.getOperationStatistics().getPerformanceByCategory().entrySet());
            categories.sort(Map.Entry.comparingByKey());
            categoryModel.setData(categories);

            List<SingleOperationPerformanceInformationType> operations = new ArrayList<>(
                    node.getOperationStatistics().getPerformanceByOperation().getOperation());
            operations.sort(Comparator.comparing(SingleOperationPerformanceInformationType::getName));
            operationModel.setData(operations);
        } else {
            samples = 0;
            categoryModel.setData(new ArrayList<>());
            operationModel.setData(new ArrayList<>());
        }
    }

    private String getInvocationsPerSample(PerformanceCategoryInfo info) {
        return String.format("%,.2f", (double) info.getTotalCount() / samples);
    }

    private String getTimePerSample(PerformanceCategoryInfo info) {
        return String.format("%,.2f", (double) info.getTotalTime() / samples / 1000.0);
    }

    private String getInvocationsPerSample(SingleOperationPerformanceInformationType info) {
        return String.format("%,.2f", (double) info.getInvocationCount() / samples);
    }

    private String getTimePerSample(SingleOperationPerformanceInformationType info) {
        return String.format("%,.2f", (double) info.getTotalTime() / samples / 1000.0);
    }

    private String getTimePerInvocation(SingleOperationPerformanceInformationType info) {
        return String.format("%,.2f", (double) info.getTotalTime() / info.getInvocationCount() / 1000.0);
    }
}
