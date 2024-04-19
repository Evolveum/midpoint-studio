package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.util.ui.tree.AbstractTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;

public class DefaultTreeModel<T> extends AbstractTreeModel {

    private DefaultMutableTreeNode root;

    private T data;

    public DefaultTreeModel() {
    }

    public DefaultTreeModel(T data) {
        this.data = data;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((DefaultMutableTreeNode) parent).getChildAt(index);
    }

    @Override
    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    protected void setRoot(@NotNull DefaultMutableTreeNode root) {
        this.root = root;
    }

    @Override
    public int getChildCount(Object parent) {
        return ((DefaultMutableTreeNode) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((DefaultMutableTreeNode) node).isLeaf();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((DefaultMutableTreeNode) parent).getIndex((DefaultMutableTreeNode) child);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
