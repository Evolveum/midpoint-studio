package com.evolveum.midpoint.studio.ui.trace.lensContext;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.ui.trace.lens.LensContextNode;
import com.evolveum.midpoint.studio.ui.trace.lens.PrismNode;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ClockworkTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the lens context (input/output) of the selected OpNode as a tree table.
 */
public class TraceLensContextPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(TraceLensContextPanel.class);

    private static final String LABEL_DEFAULT_TEXT = "Prism object: ";

    private JLabel label;
    private DefaultTreeTable<LensContextTableModel> table;
    private LensContextTableModel tableModel;

    public TraceLensContextPanel(Project project) {
        initLayout();

        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifier() {

            @Override
            public void selectedTraceNodeChange(OpNode node) {
                nodeChange(node);
            }
        });
    }

    private void initLayout() {
        label = new JLabel(LABEL_DEFAULT_TEXT);
        label.setBorder(JBUI.Borders.empty(5));

        tableModel = new LensContextTableModel();
        table = new DefaultTreeTable<>(tableModel);
        table.getTree().setRootVisible(false);

        JComponent toolbar = initMainToolbar();

        add(toolbar, BorderLayout.NORTH);
        add(new JBScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> collapseAll());
        group.add(collapseAll);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceLensContextToolbar", group, true);
        toolbar.setTargetComponent(this);
        return toolbar.getComponent();
    }

    private void expandAll() {
        for (int i = 0; i < table.getRowCount(); i++) {
            table.getTree().expandRow(i);
        }
    }

    private void collapseAll() {
        for (int i = table.getRowCount() - 1; i >= 0; i--) {
            table.getTree().collapseRow(i);
        }
    }

    private void nodeChange(OpNode node) {
        List<PrismNode> roots = new ArrayList<>();

        if (node == null) {
            label.setText(LABEL_DEFAULT_TEXT);
            tableModel.setRoots(roots);
            return;
        }

        LensContextType inputContext;
        LensContextType outputContext;

        ClockworkTraceType trace = node.getTrace(ClockworkTraceType.class);
        if (trace != null) {
            inputContext = trace.getInputLensContext();
            outputContext = trace.getOutputLensContext();
        } else {
            inputContext = null;
            outputContext = null;
        }

        if (inputContext != null || outputContext != null) {
            OperationResultType result = node.getResult();
            label.setText(result.getOperation() + " (" + result.getInvocationId() + "): " + node.getTraceNames());

            roots.add(parseContext("input", inputContext));
            roots.add(parseContext("output", outputContext));
        }

        LOG.debug("Structure changed, setting new roots");
        tableModel.setRoots(roots);
    }

    private LensContextNode parseContext(String contextLabel, LensContextType context) {
        return new LensContextNode(contextLabel, context);
    }
}
