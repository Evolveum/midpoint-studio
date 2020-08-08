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
public class OpNodeDetailsPanel extends OpNodeTreeViewPanel {

    private static final Logger LOG = Logger.getInstance(OpNodeDetailsPanel.class);

    public OpNodeDetailsPanel(@NotNull Project project) {
        super(project);
    }

    @Override
    void updateModel(OpNode node) {
        Node result;
        Node trace;

        if (node != null) {
            result = new ResultNode(node);
            trace = createTraceNodeTreatingExceptions(node);
        } else {
            result = TextNode.create("Result", "", null);
            trace = TextNode.create("Trace", "", null);
        }

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
        root.add(result);
        root.add(trace);
        updateTreeModel(root);
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

}
