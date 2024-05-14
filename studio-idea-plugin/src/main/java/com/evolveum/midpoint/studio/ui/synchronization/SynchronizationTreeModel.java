package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.ui.DefaultTreeModel;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.tree.TreePathUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;

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

        CheckedTreeNode root = createNode(NODE_ROOT);
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

            CheckedTreeNode node = createNode(item);

            if (objects.size() > 1) {
                List<CheckedTreeNode> children = new ArrayList<>();

                for (SynchronizationObjectItem object : objects) {
                    children.add(createNode(object));
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

    private CheckedTreeNode createNode(Object userObject) {
        CheckedTreeNode node = new CheckedTreeNode(userObject);
        node.setChecked(false);

        return node;
    }

    public String convertValueToText(Object userObject) {
        if (userObject == null) {
            return "<<null>>";
        }

        if (!(userObject instanceof SynchronizationItem si)) {
            return userObject.toString();
        }

        List<String> parts = new ArrayList<>();
        if (si.hasLocalChanges()) {
            parts.add("local");
        }
        if (si.hasRemoteChanges()) {
            parts.add("remote");
        }
        String changes = "";
        if (!parts.isEmpty()) {
            changes = " (Pending " + StringUtils.join(parts, "/") + ")";
        }

        return si.getName() + changes;
    }

    public void nodesRemoved(Object[] userObjects) {
        // todo implement
    }

    public void nodesChanged(Object[] userObjects) {
        Set<Object> set = new HashSet<>();
        set.addAll(Arrays.asList(userObjects));

        nodeChanged(getRoot(), set);
    }

    private void nodeChanged(DefaultMutableTreeNode node, Set<Object> changedUserObjects) {
        if (node == null) {
            return;
        }

        boolean fireChange = false;

        Object userObject = node.getUserObject();
        if (changedUserObjects.contains(node.getUserObject())) {
            fireChange = true;
        } else if (userObject instanceof SynchronizationFileItem file) {
            List<SynchronizationObjectItem> objects = file.getObjects();
            if (objects.size() == 1 && changedUserObjects.contains(objects.get(0))) {
                fireChange = true;
            }
        }

        if (fireChange) {
            TreeNode parent = node.getParent();
            treeNodesChanged(TreePathUtil.toTreePath(parent), new int[]{parent.getIndex(node)}, new Object[]{node});
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            nodeChanged(child, changedUserObjects);
        }
    }
}
