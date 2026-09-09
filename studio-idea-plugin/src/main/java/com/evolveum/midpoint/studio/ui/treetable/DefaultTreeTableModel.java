package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;

public class DefaultTreeTableModel<T> extends DefaultTreeModel implements TreeTableModel {

    private TreeTableTree tree;

    private List<ColumnInfo> columns;

    private T data;

    private final Object originalRoot;
    private Object filteredRoot;

    private RowStyleProvider rowStyleProvider;

    public DefaultTreeTableModel() {
        super(new DefaultMutableTreeNode());

        originalRoot = getRoot();
        filteredRoot = getRoot();
    }

    public DefaultTreeTableModel(@NotNull List<ColumnInfo> columns) {
        super(new DefaultMutableTreeNode());

        this.columns = columns;

        originalRoot = getRoot();
        filteredRoot = getRoot();
    }

    public void setColumns(@NotNull List<ColumnInfo> columns) {
        this.columns = columns;
    }

    public ColumnInfo getColumnInfo(int index) {
        return columns.get(index);
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public @NlsContexts.ColumnName String getColumnName(int column) {
        return columns.get(column).getName();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columns.get(column).getColumnClass();
    }

    @Override
    public Object getValueAt(Object node, int column) {
        return columns.get(column).valueOf(node);
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return columns.get(column).isCellEditable(node);
    }

    @Override
    public void setValueAt(Object aValue, Object node, int column) {
        columns.get(column).setValue(node, aValue);
    }

    public TreeTableTree getTree() {
        return tree;
    }

    @Override
    public void setTree(JTree tree) {
        if (!(tree instanceof TreeTableTree)) {
            throw new IllegalArgumentException("Tree must be instance of TreeTableTree");
        }
        this.tree = (TreeTableTree) tree;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void applyFilter(String searchText) {
        String lower = searchText == null ? "" : searchText.toLowerCase();

        if (lower.isEmpty()) {
            filteredRoot = originalRoot;
            setRoot((TreeNode) originalRoot);
            reload();
            return;
        }

        filteredRoot = filterNode((DefaultMutableTreeNode) originalRoot, node -> {
                    for (ColumnInfo column : columns) {
                        if (column instanceof FilterableColumnInfo<?, ?> filterableColumnInfo) {
                            Object value = filterableColumnInfo.valueOf((MutableTreeNode) node);
                            if (value instanceof String s) {
                                if (s.toLowerCase().contains(lower)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                });

        setRoot(filteredRoot != null
                ? (TreeNode) filteredRoot
                : new DefaultMutableTreeNode("No results of filtering"));

        reload();
    }

    private DefaultMutableTreeNode filterNode(DefaultMutableTreeNode node, Predicate<DefaultMutableTreeNode> filter) {
        boolean matches = filter.test(node);

        DefaultMutableTreeNode filteredNode = new DefaultMutableTreeNode(node.getUserObject());

        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) children.nextElement();

            DefaultMutableTreeNode filteredChild =
                    filterNode(child, filter);

            if (filteredChild != null) {
                filteredNode.add(filteredChild);
            }
        }

        if (matches || filteredNode.getChildCount() > 0) {
            return filteredNode;
        }

        return null;
    }


    public RowStyleProvider getRowStyler() {
        return rowStyleProvider;
    }

    public void setRowStyler(RowStyleProvider rowStyleProvider) {
        this.rowStyleProvider = rowStyleProvider;
    }
}
