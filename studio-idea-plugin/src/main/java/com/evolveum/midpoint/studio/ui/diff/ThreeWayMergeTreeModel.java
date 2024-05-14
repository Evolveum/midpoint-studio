package com.evolveum.midpoint.studio.ui.diff;


import com.evolveum.midpoint.schema.delta.ThreeWayMerge;
import com.evolveum.midpoint.studio.ui.DefaultTreeModel;

import javax.swing.tree.DefaultMutableTreeNode;

public class ThreeWayMergeTreeModel extends DefaultTreeModel<ThreeWayMerge> {

    private static final Object NODE_ROOT = new Object();

    @Override
    public void setData(ThreeWayMerge data) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(NODE_ROOT);

        // todo implement


        super.setData(data);

        setRoot(root);
    }
}
