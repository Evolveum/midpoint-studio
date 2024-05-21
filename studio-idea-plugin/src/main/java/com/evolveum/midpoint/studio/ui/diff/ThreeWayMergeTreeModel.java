package com.evolveum.midpoint.studio.ui.diff;


import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.delta.*;
import com.evolveum.midpoint.studio.ui.DefaultTreeModel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreeWayMergeTreeModel<O extends ObjectType> extends DefaultTreeModel<ThreeWayMerge<O>> {

    private static final Object NODE_ROOT = new Object();

    @Override
    public void setData(ThreeWayMerge<O> data) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(NODE_ROOT);

        // todo implement

        Map<ItemPath, DefaultMutableTreeNode> nodes = new HashMap<>();

        createTree(root, data.getLeftDelta(), ThreeWayMergeDirection.LEFT_TO_RIGHT, nodes);
        createTree(root, data.getRightDelta(), ThreeWayMergeDirection.RIGHT_TO_LEFT, nodes);


        super.setData(data);

        setRoot(root);
    }

    private void createTree(
            DefaultMutableTreeNode root, ObjectTreeDelta<O> objectDelta, ThreeWayMergeDirection direction,
            Map<ItemPath, DefaultMutableTreeNode> nodes) {

        if (objectDelta == null) {
            return;
        }

        createItem(root, objectDelta, direction, nodes);
    }

    private void createItem(
            DefaultMutableTreeNode parent, ItemTreeDelta itemDelta, ThreeWayMergeDirection direction,
            Map<ItemPath, DefaultMutableTreeNode> nodes) {

        if (itemDelta == null) {
            return;
        }

        DefaultMutableTreeNode node = nodes.get(itemDelta.getPath());
        if (node == null) {
            node = new DefaultMutableTreeNode(new ThreeWayMergeNode(itemDelta, direction));
            parent.add(node);

            nodes.put(itemDelta.getPath(), node);
        }


        for (ItemTreeDeltaValue dv : (List<ItemTreeDeltaValue>) itemDelta.getValues()) {
            createItemValue(node, dv, direction, nodes);
        }
    }

    private void createItemValue(
            DefaultMutableTreeNode parent, ItemTreeDeltaValue deltaValue, ThreeWayMergeDirection direction,
            Map<ItemPath, DefaultMutableTreeNode> nodes) {

        if (deltaValue == null) {
            return;
        }

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ThreeWayMergeNode(deltaValue, direction));
        parent.add(node);

        if (!(deltaValue instanceof ContainerTreeDeltaValue<?> cdv)) {
            return;
        }

        for (ItemTreeDelta delta : cdv.getDeltas()) {
            createItem(node, delta, direction, nodes);
        }
    }
}
