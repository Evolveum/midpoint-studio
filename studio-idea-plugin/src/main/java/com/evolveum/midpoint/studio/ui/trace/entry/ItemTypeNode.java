package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ItemType;
import javax.swing.tree.DefaultMutableTreeNode;

public class ItemTypeNode extends Node<ItemType> {

    public ItemTypeNode(String label, ItemType item) throws SchemaException {
        super(item);

        setLabel(label);
        setValue(TraceUtils.prettyPrint(item));

        createChildren();
    }

    private void createChildren() throws SchemaException {
        ItemType item = getUserObject();

        if (item != null && item.getValue().size() > 1) {
            for (int i = 0; i < item.getValue().size(); i++) {
                PrismValueNode.create("#" + i, item.getValue().get(i), this);
            }
        }
    }

    public static ItemTypeNode create(String label, ItemType value, DefaultMutableTreeNode parent) throws SchemaException {
        ItemTypeNode node = new ItemTypeNode(label, value);
        if (parent != null) {
            parent.add(node);
        }
        return node;
    }
}
