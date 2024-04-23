package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.tree.TreePathUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SynchronizationTreeModel extends DefaultTreeModel<List<SynchronizationFile>> {

    private static final Object NODE_ROOT = "Root";

    @Override
    public void setData(List<SynchronizationFile> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        super.setData(new ArrayList<>());

        CheckedTreeNode root = new CheckedTreeNode(NODE_ROOT);
        setRoot(root);

        addTreeNodes(root, data);

        treeStructureChanged(TreePathUtil.toTreePath(root), new int[]{0}, new Object[]{root});
    }

    public void addData(@NotNull List<SynchronizationFile> data) {
        CheckedTreeNode root = (CheckedTreeNode) getRoot();

        List<CheckedTreeNode> nodes = addTreeNodes(root, data);

        int[] indices = new int[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            indices[i] = root.getIndex(nodes.get(i));
        }

        // todo this does not work
//        treeNodesInserted(TreePathUtil.toTreePath(root), indices, nodes.toArray());
        treeStructureChanged(TreePathUtil.toTreePath(root), new int[]{0}, new Object[]{root});
    }

    private List<CheckedTreeNode> addTreeNodes(CheckedTreeNode root, List<SynchronizationFile> items) {
        List<CheckedTreeNode> nodes = new ArrayList<>();

        for (SynchronizationFile item : items) {
            getData().add(item);

            List<SynchronizationObject> objects = item.getObjects();
            if (objects.isEmpty()) {
                continue;
            }

            CheckedTreeNode node = new CheckedTreeNode(item);
            root.add(node);

            if (objects.size() > 1) {
                for (SynchronizationObject object : objects) {
                    node.add(new CheckedTreeNode(object));
                }
            }

            nodes.add(node);
        }

        return nodes;
    }
}
