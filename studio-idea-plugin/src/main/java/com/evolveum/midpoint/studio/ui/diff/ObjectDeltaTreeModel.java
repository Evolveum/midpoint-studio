package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.studio.ui.DefaultTreeModel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Collection;

public class ObjectDeltaTreeModel<O extends ObjectType> extends DefaultTreeModel<ObjectDeltaTreeData<O>> {

    private static final Object NODE_ROOT = "";

    public ObjectDeltaTreeModel() {
        setRoot(new DefaultMutableTreeNode(NODE_ROOT));
    }

    @Override
    public void setData(@NotNull ObjectDeltaTreeData<O> data) {
        PrismObject<O> target = data.target();

        ObjectDelta<O> delta = data.delta();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(NODE_ROOT);

        if (delta.isEmpty()) {
            super.setData(data);

            setRoot(root);
            return;
        }

        DefaultMutableTreeNode all = new DefaultMutableTreeNode(new AllTreeNode<>(data));
        root.add(all);

        Collection<? extends ItemDelta<?, ?>> modifications = delta.getModifications();
        for (ItemDelta<?, ?> modification : modifications) {
            DefaultMutableTreeNode itemDeltaNode =
                    new DefaultMutableTreeNode(new ItemDeltaTreeNode(modification, target));
            all.add(itemDeltaNode);

            addValues(itemDeltaNode, modification, ModificationType.ADD, modification.getValuesToAdd());
            addValues(itemDeltaNode, modification, ModificationType.DELETE, modification.getValuesToDelete());
            addValues(itemDeltaNode, modification, ModificationType.REPLACE, modification.getValuesToReplace());
        }

        super.setData(data);

        setRoot(root);
    }

    private void addValues(
            DefaultMutableTreeNode parent, ItemDelta<?, ?> itemDelta, ModificationType modificationType,
            Collection<? extends PrismValue> values) {

        if (values == null) {
            return;
        }

        values.forEach(v -> parent.add(new DefaultMutableTreeNode(new ItemDeltaValueTreeNode(itemDelta, modificationType, v))));
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

        if (parent.isLeaf()) {
            removeNodeFromParent(parent);
        }
    }
}
