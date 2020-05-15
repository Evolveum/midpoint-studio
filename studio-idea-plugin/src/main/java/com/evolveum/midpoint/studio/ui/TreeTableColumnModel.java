package com.evolveum.midpoint.studio.ui;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class TreeTableColumnModel<K, V> extends AbstractTreeTableModel {

    private List<TreeTableColumnDefinition<K, V>> columns;

    public TreeTableColumnModel(K root, @NotNull List<TreeTableColumnDefinition<K, V>> columns) {
        super(root);

        this.columns = columns;
    }

    public void setRoot(TreeTableNode root) {
        this.root = root;

        modelSupport.fireNewRoot();
    }

    public List<TreeTableColumnDefinition<K, V>> getColumns() {
        return columns;
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
    public Object getValueAt(Object object, int column) {
        if (columns.get(column).getValue() == null) {
            return null;
        }

        TreeTableNode node = (TreeTableNode) object;
        Object obj = node.getUserObject();

        return columns.get(column).getValue().apply((K) obj);
    }

    @Override
    public Object getChild(Object parent, int index) {
        TreeTableNode node = (TreeTableNode) parent;
        return node.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        TreeTableNode node = (TreeTableNode) parent;
        return node.getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeTableNode node = (TreeTableNode) parent;
        return node.getIndex((TreeTableNode) child);
    }
}
