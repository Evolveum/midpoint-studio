package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.schema.traces.PerformanceCategoryInfo;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SingleOperationPerformanceInformationType;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.evolveum.midpoint.studio.util.MidPointUtils.formatTime;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OpNodePerformancePanel extends BorderLayoutPanel {

    private JBTable category;

    private JBTable operation;

    public OpNodePerformancePanel(MessageBus bus) {
        initLayout();

        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void selectedTraceNodeChange(OpNode node) {
                nodeChange(node);
            }
        });
    }

    private void nodeChange(OpNode node) {
        if (node == null) {
            getTableModel(category).setData(new ArrayList());
            getTableModel(operation).setData(new ArrayList());
            return;
        }

        List<Map.Entry<PerformanceCategory, PerformanceCategoryInfo>> categories = new ArrayList<>(node.getPerformanceByCategory().entrySet());
        categories.sort(Comparator.comparing(e -> e.getKey()));
        getTableModel(category).setData(categories);

        List<SingleOperationPerformanceInformationType> operations = new ArrayList<>(node.getPerformance().getOperation());
        operations.sort(Comparator.comparing(SingleOperationPerformanceInformationType::getName));
        getTableModel(operation).setData(operations);
    }

    private <T> ListTableModel getTableModel(JBTable table) {
        return (ListTableModel<T>) table.getModel();
    }

    private void initLayout() {
        JBSplitter split = new OnePixelSplitter(false);

        List<TreeTableColumnDefinition<Map.Entry<PerformanceCategory, PerformanceCategoryInfo>, ?>> categoryColumns = new ArrayList<>();
        categoryColumns.add(new TreeTableColumnDefinition<>("Category", 200, o -> o.getKey().getLabel()));
        categoryColumns.add(new TreeTableColumnDefinition<>("Total #", 50, o -> o.getValue().getTotalCount()));
        categoryColumns.add(new TreeTableColumnDefinition<>("Total time", 70, o -> formatTime(o.getValue().getTotalTime())));
        categoryColumns.add(new TreeTableColumnDefinition<>("Own #", 50, o -> o.getValue().getOwnCount()));
        categoryColumns.add(new TreeTableColumnDefinition<>("Own time", 70, o -> formatTime(o.getValue().getOwnTime())));

        this.category = new JBTable(new ListTableModel(categoryColumns, new ArrayList<>()));
        split.setFirstComponent(new JBScrollPane(category));

        List<TreeTableColumnDefinition<SingleOperationPerformanceInformationType, ?>> operationColumns = new ArrayList<>();
        operationColumns.add(new TreeTableColumnDefinition<>("Operation", 500, o -> o.getName()));
        operationColumns.add(new TreeTableColumnDefinition<>("Count", 50, o -> o.getInvocationCount()));
        operationColumns.add(new TreeTableColumnDefinition<>("Total time", 100, o -> formatTime(o.getTotalTime())));
        operationColumns.add(new TreeTableColumnDefinition<>("Min", 50, o -> formatTime(o.getMinTime())));
        operationColumns.add(new TreeTableColumnDefinition<>("Max", 50, o -> formatTime(o.getMaxTime())));
        operationColumns.add(new TreeTableColumnDefinition<>("Avg", 50, o -> formatTime(o.getTotalTime() / o.getInvocationCount())));

        this.operation = new JBTable(new ListTableModel<>(operationColumns, new ArrayList<>()));
        split.setSecondComponent(new JBScrollPane(operation));

        add(split, BorderLayout.CENTER);
    }
}
