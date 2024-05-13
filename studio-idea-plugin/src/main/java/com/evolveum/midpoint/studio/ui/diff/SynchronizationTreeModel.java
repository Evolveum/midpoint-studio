package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.studio.ui.DefaultTreeModel;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationFileItem;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationItem;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationObjectItem;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.tree.TreePathUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SynchronizationTreeModel extends DefaultTreeModel<List<SynchronizationFileItem>> {

    private static final Object NODE_ROOT = "Root";

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();

        node.setUserObject(newValue);

        TreeNode parent = node.getParent();
        if (parent != null) {
            treeNodesChanged(TreePathUtil.toTreePath(parent), new int[]{parent.getIndex(node)}, new Object[]{node});
        } else {
            treeNodesChanged(null, new int[]{0}, new Object[]{node});
        }
    }

    @Override
    public void setData(List<SynchronizationFileItem> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        super.setData(new ArrayList<>());

        CheckedTreeNode root = new CheckedTreeNode(NODE_ROOT);
        setRoot(root);

        addTreeNodes(root, data);

        treeStructureChanged(TreePathUtil.toTreePath(root), new int[]{0}, new Object[]{root});
    }

    public void addFiles(@NotNull List<SynchronizationFileItem> data) {
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

    private List<CheckedTreeNode> addTreeNodes(CheckedTreeNode root, List<SynchronizationFileItem> items) {
        List<CheckedTreeNode> nodes = new ArrayList<>();

        for (SynchronizationFileItem item : items) {
            getData().add(item);

            List<SynchronizationObjectItem> objects = item.getObjects();
            if (objects.isEmpty()) {
                continue;
            }

            CheckedTreeNode node = new CheckedTreeNode(item);

            if (objects.size() > 1) {
                List<CheckedTreeNode> children = new ArrayList<>();

                for (SynchronizationObjectItem object : objects) {
                    children.add(new CheckedTreeNode(object));
                }

                children.sort(Comparator.comparing(o -> convertValueToText(o.getUserObject())));
                children.forEach(node::add);
            }

            nodes.add(node);
        }

        nodes.sort(Comparator.comparing(o -> convertValueToText(o.getUserObject())));
        nodes.forEach(root::add);

        return nodes;
    }

    public String convertValueToText(Object userObject) {
        if (userObject instanceof SynchronizationItem si) {
            return si.getName();
        }

        return userObject != null ? userObject.toString() : "<<null>>";
    }

    public void nodesChanged(Object[] nodes) {
        for (Object node : nodes) {
            if (!(node instanceof DefaultMutableTreeNode tn)) {
                continue;
            }

            TreeNode parent = tn.getParent();
            treeNodesChanged(TreePathUtil.toTreePath(parent), new int[]{parent.getIndex(tn)}, new Object[]{node});
        }
    }
}
