package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import java.util.List;

public class ItemDeltaTypeListNode extends Node<List<ItemDeltaType>> {

    public ItemDeltaTypeListNode(String label, List<ItemDeltaType> itemDeltas) throws SchemaException {
        super(itemDeltas);

        setLabel(label);
        setValue(TraceUtils.prettyPrintCollection(itemDeltas));

        createChildren();
    }

    private void createChildren() throws SchemaException {
        List<ItemDeltaType> itemDeltaList = getUserObject();

        if (itemDeltaList != null) {
            for (ItemDeltaType itemDelta : itemDeltaList) {
                ItemDeltaTypeNode.create(getLabel(), itemDelta, false, this);
            }
        }
    }

    public static ItemDeltaTypeListNode create(String label, List<ItemDeltaType> value, AbstractMutableTreeTableNode parent) throws SchemaException {
        ItemDeltaTypeListNode node = new ItemDeltaTypeListNode(label, value);
        if (parent != null) {
            parent.add(node);
        }
        return node;
    }
}
