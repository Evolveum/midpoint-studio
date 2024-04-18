package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.util.ui.tree.AbstractTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;

public class ObjectDeltaTreeModel<O extends ObjectType> extends AbstractTreeModel {

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
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("All");
        Collection<? extends ItemDelta<?, ?>> modifications = delta.getModifications();
        for (ItemDelta<?, ?> modification : modifications) {
            DefaultMutableTreeNode modificationNode = new DefaultMutableTreeNode(modification.getPath().toString());
            root.add(modificationNode);

            if (modification.getValuesToAdd() != null) {
                for (PrismValue value : modification.getValuesToAdd()) {
                    modificationNode.add(
                            new DefaultMutableTreeNode("+" + value.debugDump()));
                }
            }

            if (modification.getValuesToDelete() != null) {
                for (PrismValue value : modification.getValuesToDelete()) {
                    modificationNode.add(
                            new DefaultMutableTreeNode("-" + value.debugDump()));
                }
            }

            if (modification.getValuesToReplace() != null) {
                for (PrismValue value : modification.getValuesToReplace()) {
                    modificationNode.add(
                            new DefaultMutableTreeNode("!" + value.debugDump()));
                }
            }
        }

        this.root = root;
    }
}
