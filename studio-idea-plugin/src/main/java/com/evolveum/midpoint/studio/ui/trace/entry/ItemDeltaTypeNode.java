package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import java.util.Collections;
import java.util.List;

public class ItemDeltaTypeNode extends Node<ItemDeltaType> {

    public ItemDeltaTypeNode(String label, ItemDeltaType itemDelta, boolean showPathInValue) throws SchemaException {
        super(itemDelta);

        setLabel(label);
        setValue(TraceUtils.prettyPrint(itemDelta, showPathInValue));

        createChildren();
    }

    private void createChildren() throws SchemaException {
        ItemDeltaType idi = getUserObject();

        if (idi != null) {
            List<?> values;
            if (idi.getValue().isEmpty() && idi.getModificationType() == ModificationTypeType.REPLACE) {
                values = Collections.singletonList("");
            } else {
                values = idi.getValue();
            }
            for (Object value : values) {
                PrismValueNode.create(getLabel() + " " + idi.getModificationType(), value, this);
            }
            if (!idi.getEstimatedOldValue().isEmpty()) {
                TextNode oldRoot = TextNode.create(getLabel() + " old", TraceUtils.prettyPrintCollection(idi.getEstimatedOldValue()), this);
                for (RawType oldValue : idi.getEstimatedOldValue()) {
                    PrismValueNode.create(getLabel() + " old value", oldValue, oldRoot);
                }
            }
        }
    }

    public static ItemDeltaTypeNode create(String label, ItemDeltaType value, boolean showPathInValue, AbstractMutableTreeTableNode parent) throws SchemaException {
        ItemDeltaTypeNode node = new ItemDeltaTypeNode(label, value, showPathInValue);
        if (parent != null) {
            parent.add(node);
        }
        return node;
    }
}
