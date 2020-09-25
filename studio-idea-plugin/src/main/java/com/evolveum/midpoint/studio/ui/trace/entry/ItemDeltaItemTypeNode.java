package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaItemType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

public class ItemDeltaItemTypeNode extends Node<ItemDeltaItemType> {

    public ItemDeltaItemTypeNode(String label, ItemDeltaItemType itemDeltaItem) throws SchemaException {
        super(itemDeltaItem);

        setLabel(label);
        setValue(TraceUtils.prettyPrint(itemDeltaItem));

        createChildren();
    }

    private void createChildren() throws SchemaException {
        ItemDeltaItemType idi = getUserObject();

        if (idi != null) {
            if (idi.getOldItem() != null) {
                for (Object value : idi.getOldItem().getValue()) {
                    PrismValueNode.create("Old", value, this);
                }
            }
            for (ItemDeltaType delta : idi.getDelta()) {
                ItemDeltaTypeNode.create("Delta", delta, false, this);
            }
            if (idi.getNewItem() != null) {
                for (Object value : idi.getNewItem().getValue()) {
                    PrismValueNode.create("New", value, this);
                }
            }
        }
    }

    public static ItemDeltaItemTypeNode create(String label, ItemDeltaItemType value, AbstractMutableTreeTableNode parent) throws SchemaException {
        ItemDeltaItemTypeNode node = new ItemDeltaItemTypeNode(label, value);
        if (parent != null) {
            parent.add(node);
        }
        return node;
    }
}
