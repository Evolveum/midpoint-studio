package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.List;

public class DefaultTreeTableModel<T> extends DefaultTreeModel implements TreeTableModel {

    private TreeTableTree tree;

    private List<ColumnInfo> columns;

    private T data;

    public DefaultTreeTableModel() {
        super(new DefaultMutableTreeNode());
    }

    public DefaultTreeTableModel(@NotNull List<ColumnInfo> columns) {
        super(new DefaultMutableTreeTableNode());

        this.columns = columns;
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
}
