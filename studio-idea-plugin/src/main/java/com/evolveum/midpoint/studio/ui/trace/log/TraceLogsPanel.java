package com.evolveum.midpoint.studio.ui.trace.log;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LogSegmentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceLogsPanel extends BorderLayoutPanel {

    private JBTextArea logs;
    private SimpleCheckboxAction currentOpOnly;
    private SimpleCheckboxAction logsShowSegmentSeparators;
    private SimpleCheckboxAction alwaysLoadFully;

    private OpNode currentOpNode;

    private boolean fullyLoaded;

    private static final int LOAD_AUTOMATICALLY = 100000;

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

        updateTexts(alwaysLoadFully.isSelected());
    }

    private void initLayout() {
        DefaultActionGroup group = new DefaultActionGroup();
        SimpleCheckboxAction logsWrapText = new SimpleCheckboxAction("Wrap text") {
            @Override
            public void onStateChange() {
                logs.setLineWrap(isSelected());
                updateTexts(fullyLoaded);
            }
        };
        group.add(logsWrapText);

        currentOpOnly = new SimpleCheckboxAction("Current operation only") {
            @Override
            public void onStateChange() {
                updateTexts(fullyLoaded);
            }
        };
        group.add(currentOpOnly);

        logsShowSegmentSeparators = new SimpleCheckboxAction("Show segment separators") {

            @Override
            public void onStateChange() {
                updateTexts(fullyLoaded);
            }
        };
        group.add(logsShowSegmentSeparators);

        alwaysLoadFully = new SimpleCheckboxAction("Always load fully");
        group.add(alwaysLoadFully);

        AnAction load = new AnAction("Load", "Load full log", AllIcons.Actions.Show) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateTexts(true);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(!fullyLoaded);
            }
        };
        group.add(load);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewLogsToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

        logs = new JBTextArea();
        JBScrollPane logsScrollPane = new JBScrollPane(logs);
        add(logsScrollPane);
    }

    private void updateTexts(boolean loadFully) {
        long start = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        if (currentOpNode != null) {
            collectLogEntries(sb, currentOpNode, loadFully);
        }

        System.out.println("Log prepared in " + (System.currentTimeMillis() - start) + " ms");
        logs.setText(sb.toString());
        logs.setCaretPosition(0);
        System.out.println("All done. In " + (System.currentTimeMillis() - start) + " ms");
    }

    public void collectLogEntries(StringBuilder sb, OpNode node, boolean loadFully) {
        long start = System.currentTimeMillis();

        System.out.println("collectLogEntries started for " + node);

        java.util.List<LogSegment> allSegments = new ArrayList<>();
        collectLogSegments(allSegments, node);
        System.out.println("Collect log segments finished: " + (System.currentTimeMillis() - start) + " ms after start; " + allSegments.size() + " segments");

        allSegments.sort(Comparator.comparing(seg -> seg.segment.getSequenceNumber()));

        System.out.println("Collect log segments sorted: " + (System.currentTimeMillis() - start) + " ms after start");

        fullyLoaded = true;

        main: for (LogSegment segment : allSegments) {
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
                if (!loadFully && sb.length() >= LOAD_AUTOMATICALLY) {
                    sb.append("... Please click 'load' to get the rest of the log.");
                    fullyLoaded = false;
                    break main;
                }
            }
        }

        System.out.println("Content prepared: " + (System.currentTimeMillis() - start) + " ms after start; " + sb.length()
                + " chars. Fully loaded: " + fullyLoaded);
    }

    private void collectLogSegments(List<LogSegment> allSegments, OpNode node) {
        for (LogSegmentType segment : node.getResult().getLog()) {
            allSegments.add(new LogSegment(segment, node));
        }
        if (!currentOpOnly.isSelected()) {
            node.getChildren().forEach(child -> collectLogSegments(allSegments, child));
        }
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
