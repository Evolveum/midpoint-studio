package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.compatibility.ExtendedListSelectionModel;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.impl.trace.Format;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.ui.trace.entry.Node;
import com.evolveum.midpoint.studio.ui.trace.entry.ResultNode;
import com.evolveum.midpoint.studio.ui.trace.entry.TextNode;
import com.evolveum.midpoint.studio.ui.trace.entry.TraceNode;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Displays detailed information about given OpNode: Shows operation result and (optional) trace in a tree view.
 *
 * Created by Viliam Repan (lazyman).
 */
public class OpNodeDetailsPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(OpNodeDetailsPanel.class);

    private JXTreeTable variables;

    private FormatComboboxAction variablesDisplayAs;

    private CheckboxAction variablesWrapText;

    private JBTextArea variablesValue;

    public OpNodeDetailsPanel(@NotNull Project project) {
        initLayout();

        Node resultNode = TextNode.create("Result", "", null);
        Node traceNode = TextNode.create("Trace", "", null);

        updateModel(resultNode, traceNode);

        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void selectedTraceNodeChange(OpNode node) {
                nodeChange(node);
            }
        });
    }

    private void nodeChange(OpNode node) {
        System.out.println("Node change: " + node);
        if (node == null) {
            updateModel(null, null);
        } else {
            Node result = new ResultNode(node);
            Node trace = createTraceNodeTreatingExceptions(node);
            updateModel(result, trace);
        }
        applySelection(null);
    }

    private Node createTraceNodeTreatingExceptions(OpNode node) {
        try {
            return createTraceNode(node);
        } catch (SchemaException ex) {
            LOG.error("Couldn't create trace node", ex);
            return TextNode.create("Trace", "Error: " + ex.getMessage(), null);
        }
    }

    private void applySelection(Node obj) {
        if (obj == null) {
            variablesValue.setText(null);
        } else {
            Format format = variablesDisplayAs.getFormat();
            String text = format.format(obj.getObject());
            variablesValue.setText(text);
        }
    }

    private void updateModel(Node result, Node trace) {
        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
        if (result != null) {
            root.add(result);
        }
        if (trace != null) {
            root.add(trace);
        }

        DefaultTreeTableModel model = (DefaultTreeTableModel) variables.getTreeTableModel();
        model.setRoot(root);

        this.variables.invalidate();
    }

    private Node createTraceNode(OpNode opNode) throws SchemaException {
        List<TraceType> traces = opNode.getResult().getTrace();
        if (traces.isEmpty()) {
            return TextNode.create("Trace", "(none)", null);
        } else if (traces.size() == 1) {
            return createTraceNode(traces.get(0), null);
        } else {
            TextNode rv = TextNode.create("Trace", "Number: " + traces.size(), null);
            for (TraceType trace : traces) {
                createTraceNode(trace, rv);
            }
            return rv;
        }
    }

    private Node createTraceNode(TraceType trace, TextNode parent) throws SchemaException {
        return TraceNode.create(trace, parent);
    }

    private void variableDisplayAsChanged(Format format) {
        ExtendedListSelectionModel ext = (ExtendedListSelectionModel) variables.getSelectionModel();
        int[] indices = ext.getSelectedIndices();
        if (indices == null || indices.length == 0) {
            applySelection(null);
        }

        TreeTableNode node = (TreeTableNode) variables.getPathForRow(indices[0]).getLastPathComponent();
        if (node instanceof Node) {
            applySelection((Node) node);
        } else {
            applySelection(null);
        }
    }

    private void variablesSelectionChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        TreeTableNode node = path != null ? (TreeTableNode) path.getLastPathComponent() : null;

        Node obj = null;
        if (node instanceof Node) {
            obj = (Node) node;
        }

        applySelection(obj);
    }

    private void initLayout() {
        JBSplitter splitter = new OnePixelSplitter(false);
        add(splitter, BorderLayout.CENTER);

        List<TreeTableColumnDefinition> columns = new ArrayList<>();

        columns.add(new TreeTableColumnDefinition<String, String>("Item", 150, o -> null));
        columns.add(new TreeTableColumnDefinition<String, String>("Variable", 400, o -> null));

        this.variables = MidPointUtils.createTable(new DefaultTreeTableModel(new DefaultMutableTreeTableNode(), Arrays.asList("Item", "Variable")), null);
        this.variables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.variables.addTreeSelectionListener(e -> variablesSelectionChanged(e));

        JComponent mainToolbar = initMainToolbar();

        splitter.setFirstComponent(MidPointUtils.createBorderLayoutPanel(mainToolbar, new JBScrollPane(this.variables), null));

        JPanel left = new BorderLayoutPanel();
        splitter.setSecondComponent(left);

        DefaultActionGroup group = new DefaultActionGroup();
        variablesDisplayAs = new FormatComboboxAction() {

            @Override
            public void setFormat(Format format) {
                super.setFormat(format);

                variableDisplayAsChanged(format);
            }
        };
        group.add(variablesDisplayAs);
        variablesWrapText = new SimpleCheckboxAction("Wrap text") {

            @Override
            public void onStateChange() {
                variablesValue.setLineWrap(isSelected());
                variablesValue.invalidate();
            }
        };
        group.add(variablesWrapText);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewVariablesToolbar", group, true);
        left.add(toolbar.getComponent(), BorderLayout.NORTH);

        variablesValue = new JBTextArea();
        left.add(new JBScrollPane(variablesValue), BorderLayout.CENTER);
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> variables.expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> variables.collapseAll());
        group.add(collapseAll);

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("TraceTreeToolbar", group, true);
        return resultsActionsToolbar.getComponent();
    }

    private static class FormatComboboxAction extends ComboBoxAction {

        private Format format = Format.AUTO;

        @NotNull
        @Override
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            DefaultActionGroup group = new DefaultActionGroup();

            for (Format f : Format.values()) {
                group.add(new FormatAction(f) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        FormatComboboxAction.this.setFormat(this.getFormat());
                    }
                });
            }

            return group;
        }

        @Override
        public void update(AnActionEvent e) {
            super.update(e);

            String text = getFormat().getDisplayName();
            getTemplatePresentation().setText(text);
            e.getPresentation().setText(text);
        }

        public void setFormat(Format format) {
            this.format = format;
        }

        public Format getFormat() {
            return format != null ? format : Format.AUTO;
        }
    }

    private static abstract class FormatAction extends AnAction implements DumbAware {

        private Format format;

        public FormatAction(Format format) {
            super(format.getDisplayName());
            this.format = format;
        }

        public Format getFormat() {
            return format;
        }
    }
}
