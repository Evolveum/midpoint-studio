package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LogSegmentType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceLogsPanel extends BorderLayoutPanel {

    private JBTextArea logs;
    private CheckboxAction logsWrapText;
    private CheckboxAction logsShowSegmentSeparators;

    public TraceLogsPanel() {
        initLayout();
    }

    private void initLayout() {
        DefaultActionGroup group = new DefaultActionGroup();
        logsWrapText = new SimpleCheckboxAction("Wrap text");
        group.add(logsWrapText);
        logsShowSegmentSeparators = new SimpleCheckboxAction("Show segment separators");
        group.add(logsShowSegmentSeparators);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewLogsToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

        logs = new JBTextArea();
        add(new JBScrollPane(logs));
    }



//    private void updateTexts() {
//        StringBuilder sb = new StringBuilder();
//        if (currentOpNode != null) {
//            collectLogEntries(sb, currentOpNode);
//        }
//        updateTexts(sb.toString());
//    }
//
//    public void collectLogEntries(StringBuilder sb, OpNode node) {
//        List<LogSegment> allSegments = new ArrayList<>();
//        collectLogSegments(allSegments, node);
//        allSegments.sort(Comparator.comparing(seg -> seg.segment.getSequenceNumber()));
//
//        for (LogSegment segment : allSegments) {
//            if (showSegmentSeparators.getSelection()) {
//                sb.append("---> Segment #" + segment.segment.getSequenceNumber() + " in " + segment.owner.getOperationQualified() + " (inv: " + segment.owner.getResult().getInvocationId() + ")\n");
//            }
//            for (String entry : segment.segment.getEntry()) {
//                // ugly hacking to normalize line ends
//                if (entry.endsWith("\r")) {
//                    entry = entry.substring(0, entry.length()-1);
//                }
//                String normalized = entry.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
//                sb.append(normalized).append("\n");
//            }
//        }
//    }
//
//    private void collectLogSegments(List<LogSegment> allSegments, OpNode node) {
//        for (LogSegmentType segment : node.getResult().getLog()) {
//            allSegments.add(new LogSegment(segment, node));
//        }
//        node.getChildren().forEach(child -> collectLogSegments(allSegments, child));
//    }
//
//    private static class LogSegment {
//
//        private final LogSegmentType segment;
//
//        private final OpNode owner;
//
//        public LogSegment(LogSegmentType segment, OpNode owner) {
//            this.segment = segment;
//            this.owner = owner;
//        }
//    }
}
