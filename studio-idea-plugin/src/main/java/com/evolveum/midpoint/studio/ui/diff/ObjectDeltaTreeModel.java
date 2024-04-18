package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.util.ui.tree.AbstractTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Collection;

public class ObjectDeltaTreeModel<O extends ObjectType> extends AbstractTreeModel {

    public static final Object ROOT_ALL = "All";

    private ObjectDelta<O> delta;

    private DefaultMutableTreeNode root;

    public ObjectDeltaTreeModel(@NotNull ObjectDelta<O> delta) {
        setDelta(delta);
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((DefaultMutableTreeNode) parent).getChildAt(index);
    }

    @Override
    public Object getRoot() {
        return root;
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

    public void setDelta(@NotNull ObjectDelta<O> delta) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(delta);
        Collection<? extends ItemDelta<?, ?>> modifications = delta.getModifications();
        for (ItemDelta<?, ?> modification : modifications) {
            DefaultMutableTreeNode itemDeltaNode = new DefaultMutableTreeNode(modification);
            root.add(itemDeltaNode);

            addValues(itemDeltaNode, ModificationType.ADD, modification.getValuesToAdd());
            addValues(itemDeltaNode, ModificationType.DELETE, modification.getValuesToDelete());
            addValues(itemDeltaNode, ModificationType.REPLACE, modification.getValuesToReplace());
        }

        this.root = root;
    }

    private void addValues(
            DefaultMutableTreeNode parent, ModificationType modificationType,
            Collection<? extends PrismValue> values) {

        if (values == null) {
            return;
        }

        values.forEach(v -> parent.add(new DefaultMutableTreeNode(new DeltaItem(modificationType, v))));
    }


    public void removeNodeFromParent(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == null) {
            return;
        }

        TreePath path = new TreePath(parent.getPath());
        int[] indices = new int[]{parent.getIndex(node)};
        Object[] children = new Object[]{node};

        parent.remove(node);

        treeNodesRemoved(path, indices, children);
    }
}
