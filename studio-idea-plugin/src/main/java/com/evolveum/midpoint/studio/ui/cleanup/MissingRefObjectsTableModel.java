package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import java.util.*;
import java.util.stream.Collectors;

public class MissingRefObjectsTableModel extends DefaultTreeTableModel<List<MissingRefObject>> {

    public static final Object NODE_ROOT = new Object();

    public static final Object NODE_ALL = new Object();

    public MissingRefObjectsTableModel() {
        super();

        setColumns(List.of(
                new MissingRefColumn(),
                new MissingRefActionColumn(this)
        ));
    }

    @Override
    public void setData(List<MissingRefObject> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new MissingRefNode<>(NODE_ROOT));

        DefaultMutableTreeNode allNode = null;
        if (!data.isEmpty()) {
            allNode = new DefaultMutableTreeNode(new MissingRefNode<>(NODE_ALL));
            rootNode.add(allNode);
        }

        Map<QName, List<MissingRefObject>> map = data.stream()
                .collect(Collectors.groupingBy(MissingRefObject::getType));

        List<ObjectTypes> types = Arrays.asList(ObjectTypes.values());
        types.sort(MidPointUtils.OBJECT_TYPES_COMPARATOR);

        for (ObjectTypes type : types) {
            List<MissingRefObject> list = map.get(type.getTypeQName());
            if (list == null || list.isEmpty()) {
                continue;
            }

            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(new MissingRefNode<>(type));
            allNode.add(typeNode);

            for (MissingRefObject object : list) {
                DefaultMutableTreeNode objectNode = new DefaultMutableTreeNode(new MissingRefNode<>(object));
                typeNode.add(objectNode);

                for (MissingRef ref : object.getReferences()) {
                    DefaultMutableTreeNode refNode = new DefaultMutableTreeNode(ref);
                    objectNode.add(refNode);
                }
            }
        }

        setRoot(rootNode);

        super.setData(data);
    }

    public void markSelectedAction(MissingRefAction action) {
        int[] selected = getTree().getSelectionRows();
        if (selected == null || selected.length == 0) {
            return;
        }

        for (int i = 0; i < selected.length; i++) {
            TreePath path = getTree().getPathForRow(selected[i]);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            markSelectedAction(node, action);
        }
    }

    private void markSelectedAction(DefaultMutableTreeNode node, MissingRefAction action) {
        Object userObject = node.getUserObject();

        if (userObject instanceof MissingRefNode refNode) {
            refNode.setAction(action);

            for (int i = 0; i < node.getChildCount(); i++) {
                markSelectedAction((DefaultMutableTreeNode) node.getChildAt(i), action);
            }
        } else if (userObject instanceof MissingRef ref) {
            ref.setAction(action);
        }

        Object[] path = {node.getParent(), node};
        int[] childIndices = {node.getParent().getIndex(node)};
        Object[] changedChildren = {node};

        fireTreeNodesChanged(this, path, childIndices, changedChildren);
    }

    public void removeNodes(int[] selected) {
        for (int i = 0; i < selected.length; i++) {
            TreePath path = getTree().getPathForRow(selected[i]);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            removeNodeFromParent(node);

            Object userObject = node.getUserObject();
            if (userObject instanceof MissingRefNode refNode) {
                Object value = refNode.getValue();
                if (value == NODE_ALL || value == NODE_ROOT) {
                    getData().clear();
                } else if (value instanceof ObjectTypes ot) {
                    getData().removeIf(o -> Objects.equals(o.getType(), ot.getTypeQName()));
                } else if (value instanceof MissingRefObject object) {
                    getData().removeIf(o ->
                            Objects.equals(o.getOid(), object.getOid()) && Objects.equals(o.getType(), object.getType()));
                }
            } else if (userObject instanceof MissingRef ref) {
                getData().forEach(o -> o.getReferences().remove(ref));
            }
        }
    }

    public void removeNodeFromParent(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == null) {
            return;
        }

        int[] childIndex = new int[1];
        Object[] removedArray = new Object[1];

        childIndex[0] = parent.getIndex(node);
        parent.remove(childIndex[0]);
        removedArray[0] = node;
        nodesWereRemoved(parent, childIndex, removedArray);
    }
}
