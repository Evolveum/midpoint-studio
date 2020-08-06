package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LogSegmentType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceLogsPanel extends BorderLayoutPanel {

    private JBTextArea logs;
    private JBScrollPane logsScrollPane;
    private SimpleCheckboxAction logsWrapText;
    private SimpleCheckboxAction logsShowSegmentSeparators;

    private OpNode currentOpNode;

    public TraceLogsPanel(MessageBus bus) {
        initLayout();

        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void selectedTraceNodeChange(OpNode node) {
                nodeChange(node);
            }
        });
    }

    private void nodeChange(OpNode node) {
        currentOpNode = node;

        updateTexts();
    }

    private void initLayout() {
        DefaultActionGroup group = new DefaultActionGroup();
        logsWrapText = new SimpleCheckboxAction("Wrap text") {

            @Override
            public void onStateChange() {
                logs.setLineWrap(isSelected());
                updateTexts();
            }
        };
        group.add(logsWrapText);
        logsShowSegmentSeparators = new SimpleCheckboxAction("Show segment separators") {

            @Override
            public void onStateChange() {
                updateTexts();
            }
        };
        group.add(logsShowSegmentSeparators);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewLogsToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

        logs = new JBTextArea();
        logsScrollPane = new JBScrollPane(logs);
        add(logsScrollPane);
    }

    private void updateTexts() {
        StringBuilder sb = new StringBuilder();
        if (currentOpNode != null) {
            collectLogEntries(sb, currentOpNode);
        }

        logs.setText(sb.toString());
        logs.setCaretPosition(0);
    }

    public void collectLogEntries(StringBuilder sb, OpNode node) {
        java.util.List<LogSegment> allSegments = new ArrayList<>();
        collectLogSegments(allSegments, node);
        allSegments.sort(Comparator.comparing(seg -> seg.segment.getSequenceNumber()));

        for (LogSegment segment : allSegments) {
            if (logsShowSegmentSeparators.isSelected()) {
                sb.append("---> Segment #").append(segment.segment.getSequenceNumber()).append(" in ")
                        .append(segment.owner.getOperationQualified()).append(" (inv: ")
                        .append(segment.owner.getResult().getInvocationId()).append(")\n");
            }
            for (String entry : segment.segment.getEntry()) {
                // ugly hacking to normalize line ends
                if (entry.endsWith("\r")) {
                    entry = entry.substring(0, entry.length() - 1);
                }
                String normalized = entry.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
                sb.append(normalized).append("\n");
            }
        }
    }

    private void collectLogSegments(List<LogSegment> allSegments, OpNode node) {
        for (LogSegmentType segment : node.getResult().getLog()) {
            allSegments.add(new LogSegment(segment, node));
        }
        node.getChildren().forEach(child -> collectLogSegments(allSegments, child));
    }

    private static class LogSegment {

        private final LogSegmentType segment;

        private final OpNode owner;

        public LogSegment(LogSegmentType segment, OpNode owner) {
            this.segment = segment;
            this.owner = owner;
        }
    }
}
