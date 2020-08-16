package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceTreeTableModel extends DefaultTreeTableModel {

    private List<TreeTableColumnDefinition> columns;

    public TraceTreeTableModel(List<TreeTableColumnDefinition> columns, List<OpNode> nodes) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        this.columns = columns;

        List<DefaultMutableTreeTableNode> list = init(nodes);

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
        list.forEach(i -> root.add(i));
        setRoot(root);
    }

    public TreeTableColumnDefinition getColumn(int index) {
        return columns.get(index);
    }

    private List<DefaultMutableTreeTableNode> init(List<OpNode> nodes) {
        List<DefaultMutableTreeTableNode> list = new ArrayList<>();
        if (nodes == null) {
            return list;
        }

        for (OpNode node : nodes) {
            list.add(buildNode(node));
        }

        return list;
    }

    private DefaultMutableTreeTableNode buildNode(OpNode node) {
        DefaultMutableTreeTableNode n = new DefaultMutableTreeTableNode(node);

        if (node.getChildren() == null) {
            return n;
        }

        for (OpNode child : node.getChildren()) {
            n.add(buildNode(child));
        }

        return n;
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }

    @Override
    public Object getValueAt(Object node, int column) {
        DefaultMutableTreeTableNode d = (DefaultMutableTreeTableNode) node;
        if (d == null || d.getUserObject() == null) {
            return null;
        }

        return columns.get(column).getValue().apply(d.getUserObject());
    }
}
