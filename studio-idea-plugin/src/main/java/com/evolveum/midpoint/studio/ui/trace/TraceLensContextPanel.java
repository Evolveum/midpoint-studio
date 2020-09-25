package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.ui.trace.lens.LensContextNode;
import com.evolveum.midpoint.studio.ui.trace.lens.PrismNode;
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
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceLensContextPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(TraceLensContextPanel.class);

    private static final String LABEL_DEFAULT_TEXT = "Prism object: ";

    private JLabel label;
    private JXTreeTable table;

    public TraceLensContextPanel(Project project) {
        initLayout();

        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void selectedTraceNodeChange(OpNode node) {
                nodeChange(node);
            }
        });
    }

    private void initLayout() {
        label = new JLabel(LABEL_DEFAULT_TEXT);
        label.setBorder(JBUI.Borders.empty(5));
        add(label, BorderLayout.NORTH);

        List<TreeTableColumnDefinition> columns = new ArrayList<>();

        columns.add(new TreeTableColumnDefinition<String, String>("Item", 200, o -> null));
        columns.add(new TreeTableColumnDefinition<String, String>("Old", 100, o -> null));
        columns.add(new TreeTableColumnDefinition<String, String>("Current", 100, o -> null));
        columns.add(new TreeTableColumnDefinition<String, String>("New", 100, o -> null));

        this.table = MidPointUtils.createTable(new DefaultTreeTableModel(new DefaultMutableTreeTableNode(),
                Arrays.asList("Item", "Old", "Current", "New")), null);

        add(new JBScrollPane(table), BorderLayout.CENTER);

        JComponent toolbar = initMainToolbar();
        add(toolbar, BorderLayout.NORTH);
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> table.expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> table.collapseAll());
        group.add(collapseAll);

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("TraceLensContextToolbar", group, true);
        return resultsActionsToolbar.getComponent();
    }

    private void nodeChange(OpNode node) {
        List<PrismNode> roots = new ArrayList<>();

        if (node == null) {
            label.setText(LABEL_DEFAULT_TEXT);

            updateTableModel(roots);
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

        if (node != null && (inputContext != null || outputContext != null)) {
            OperationResultType result = node.getResult();
            label.setText(result.getOperation() + " (" + result.getInvocationId() + "): " + node.getTraceNames());

            roots.add(parseContext("input", inputContext));
            roots.add(parseContext("output", outputContext));

            LOG.debug("Structure changed, setting new roots");
            updateTableModel(roots);
        }
    }

    private void updateTableModel(List<PrismNode> roots) {
        DefaultTreeTableModel model = (DefaultTreeTableModel) table.getTreeTableModel();

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
        if (roots != null) {
            roots.stream().forEach(r -> root.add(r));
        }
        model.setRoot(root);
    }

    private LensContextNode parseContext(String label, LensContextType context) {
        return new LensContextNode(label, context);
    }
}
