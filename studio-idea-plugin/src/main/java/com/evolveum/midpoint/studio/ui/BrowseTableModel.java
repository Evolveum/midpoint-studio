package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.MidPointLocalizationService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class BrowseTableModel extends AbstractTreeTableModel {

    private List<TreeTableColumnDefinition<ObjectType, ?>> columns;

    private DefaultMutableTreeTableNode root;

    public BrowseTableModel(@NotNull List<TreeTableColumnDefinition<ObjectType, ?>> columns) {
        this.columns = columns;
    }

    public void fireTableDataChanged() {
        modelSupport.fireNewRoot();
    }

    @Override
    public Object getRoot() {
        return root;
    }

    public void setData(List<ObjectType> data) {
        root = new DefaultMutableTreeTableNode();

        if (data == null || data.isEmpty()) {
            return;
        }

        Map<ObjectTypes, List<ObjectType>> map = new HashMap<>();
        for (ObjectType o : data) {
            ObjectTypes type = ObjectTypes.getObjectType(o.getClass());
            List<ObjectType> list = map.get(type);
            if (list == null) {
                list = new ArrayList<>();
                map.put(type, list);
            }
            list.add(o);
        }

        List<ObjectTypes> types = new ArrayList<>();
        types.addAll(map.keySet());
        Collections.sort(types, MidPointUtils.OBJECT_TYPES_COMPARATOR);

        for (ObjectTypes t : types) {
            DefaultMutableTreeTableNode type = new DefaultMutableTreeTableNode(t);
            root.add(type);

            List<ObjectType> list = map.get(t);
            Collections.sort(list, MidPointUtils.OBJECT_TYPE_COMPARATOR);

            for (ObjectType o : list) {
                DefaultMutableTreeTableNode n = new DefaultMutableTreeTableNode(o);
                type.add(n);
            }
        }
    }

    @Override
    public Object getValueAt(Object o, int i) {
        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) o;
        Object obj = node.getUserObject();

        if (obj instanceof ObjectType) {
            return columns.get(i).getValue().apply((ObjectType) obj);
        } else if (obj instanceof ObjectTypes) {
            if (i != 0) {
                return null;
            }

            ObjectTypes type = (ObjectTypes) obj;
            String text = type.getTypeQName().getLocalPart();

            return MidPointLocalizationService.getInstance().translate("ObjectType." + text, text);
        }

        return null;
    }

    @Override
    public Object getChild(Object parent, int index) {
        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) parent;
        return node.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) parent;
        return node.getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) parent;
        return node.getIndex((DefaultMutableTreeTableNode) child);
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }
}
