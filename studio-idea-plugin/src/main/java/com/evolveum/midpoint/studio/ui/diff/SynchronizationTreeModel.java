package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.ui.tree.TreePathUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class SynchronizationTreeModel extends DefaultTreeModel<List<SynchronizationFileItem>> {

    private static final Object NODE_ROOT = "Root";

    @Override
    public void setData(List<SynchronizationFileItem> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        super.setData(new ArrayList<>());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(NODE_ROOT);
        setRoot(root);

        addTreeNodes(root, data);

        treeStructureChanged(TreePathUtil.toTreePath(root), new int[]{0}, new Object[]{root});
    }

    public void addData(@NotNull List<SynchronizationFileItem> data) {
        DefaultMutableTreeNode root = getRoot();

        List<DefaultMutableTreeNode> nodes = addTreeNodes(root, data);

        int[] indices = new int[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            indices[i] = root.getIndex(nodes.get(i));
        }

        // todo this does not work
//        treeNodesInserted(TreePathUtil.toTreePath(root), indices, nodes.toArray());
        treeStructureChanged(TreePathUtil.toTreePath(root), new int[]{0}, new Object[]{root});
    }

    private List<DefaultMutableTreeNode> addTreeNodes(DefaultMutableTreeNode root, List<SynchronizationFileItem> items) {
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();

        for (SynchronizationFileItem item : items) {
            getData().add(item);

            List<SynchronizationObjectItem> objects = item.objects();
            if (objects.isEmpty()) {
                continue;
            }

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
            root.add(node);

            if (objects.size() > 1) {
                for (SynchronizationObjectItem object : objects) {
                    node.add(new DefaultMutableTreeNode(object));
                }
            }

            nodes.add(node);
        }

        return nodes;
    }
}
