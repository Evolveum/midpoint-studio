package com.evolveum.midpoint.studio.ui.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractRoleType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;

public class ObjectsTreeTableModel extends DefaultTreeModel implements TreeTableModel {

    private TreeTableTree tree;

    private List<ObjectType> objects;

    private static final List<ColumnInfo> COLUMNS = List.of(
            new ObjectsTreeColumnInfo(),
            new ObjectColumnInfo<>("Display name", o -> {
                if (o instanceof AbstractRoleType role) {
                    return MidPointUtils.getOrigFromPolyString(role.getDisplayName());
                }

                return null;
            }),
            new ObjectColumnInfo<>("Oid", o -> {
                if (o instanceof ObjectType object) {
                    return object.getOid();
                }

                return null;
            })
    );

    public ObjectsTreeTableModel() {
        super(new DefaultMutableTreeTableNode());
    }

    public ColumnInfo getColumnInfo(int index) {
        return COLUMNS.get(index);
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.size();
    }

    @Override
    public @NlsContexts.ColumnName String getColumnName(int column) {
        return COLUMNS.get(column).getName();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMNS.get(column).getColumnClass();
    }

    @Override
    public Object getValueAt(Object node, int column) {
        return COLUMNS.get(column).valueOf(node);
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return COLUMNS.get(column).isCellEditable(node);
    }

    @Override
    public void setValueAt(Object aValue, Object node, int column) {
        COLUMNS.get(column).setValue(node, aValue);
    }

    @Override
    public void setTree(JTree tree) {
        if (!(tree instanceof TreeTableTree)) {
            throw new IllegalArgumentException("Tree must be instance of TreeTableTree");
        }
        this.tree = (TreeTableTree) tree;
    }

    public void setData(List<ObjectType> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();

        Map<ObjectTypes, List<ObjectType>> map = new HashMap<>();
        for (ObjectType o : data) {
            ObjectTypes type = ObjectTypes.getObjectType(o.getClass());
            List<ObjectType> list = map.computeIfAbsent(type, k -> new ArrayList<>());
            list.add(o);
        }

        List<ObjectTypes> types = new ArrayList<>(map.keySet());
        types.sort(MidPointUtils.OBJECT_TYPES_COMPARATOR);

        for (ObjectTypes t : types) {
            DefaultMutableTreeTableNode type = new DefaultMutableTreeTableNode(t);
            root.add(type);

            List<ObjectType> list = map.get(t);
            list.sort(MidPointUtils.OBJECT_TYPE_COMPARATOR);

            for (ObjectType o : list) {
                DefaultMutableTreeTableNode n = new DefaultMutableTreeTableNode(o);
                type.add(n);
            }
        }

        setRoot(root);
        this.objects = Collections.unmodifiableList(data);
    }

    public List<ObjectType> getObjects() {
        if (objects == null) {
            objects = Collections.emptyList();
        }
        return objects;
    }

    public List<ObjectType> getSelectedObjects() {
        List<ObjectType> selected = new ArrayList<>();

        List<ObjectType> data = getObjects();

        ListSelectionModel selectionModel = tree.getTreeTable().getSelectionModel();
        int[] indices = selectionModel.getSelectedIndices();
        for (int i : indices) {
            DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode)
                    tree.getPathForRow(i).getLastPathComponent();
            Object obj = node.getUserObject();
            if (obj instanceof ObjectTypes) {
                ObjectTypes type = (ObjectTypes) obj;
                data.forEach(o -> {
                    if (type.getClassDefinition().equals(o.getClass())) {
                        if (!selected.contains(o)) {
                            selected.add(o);
                        }
                    }
                });
            } else if (obj instanceof ObjectType) {
                ObjectType o = (ObjectType) obj;
                if (!selected.contains(o)) {
                    selected.add(o);
                }
            }
        }

        return selected;
    }

    public List<Pair<String, ObjectTypes>> getSelectedOids() {
        List<Pair<String, ObjectTypes>> selected = new ArrayList<>();

        List<ObjectType> objects = getSelectedObjects();
        objects.forEach(o -> selected.add(new Pair<>(o.getOid(), ObjectTypes.getObjectType(o.getClass()))));

        return selected;
    }
}
