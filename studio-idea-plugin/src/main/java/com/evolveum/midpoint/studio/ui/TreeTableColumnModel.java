package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TreeTableColumnModel<K, V> extends AbstractTreeTableModel {

    private List<TreeTableColumnDefinition<K, V>> columns;

    public TreeTableColumnModel(@NotNull List<TreeTableColumnDefinition<K, V>> columns) {
        this.columns = columns;
    }

    public void setRoot() {

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

    @Override
    public Object getValueAt(Object object, int column) {
        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) object;
        Object obj = node.getUserObject();

        if (!(obj instanceof ObjectType)) {
            return null;
        }

        if (obj == null) {
            return null;
        }

        if (columns.get(column).getValue() == null) {
            return null;
        }

        return columns.get(column).getValue().apply((K) obj);
    }
}
