package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import java.util.Collections;

public class ItemNode extends Node<Item<?, ?>> {

    public ItemNode(Item<?, ?> item) throws SchemaException {
        this("", item);
    }

    public ItemNode(String prefix, Item<?, ?> item) throws SchemaException {
        super(item);

        setLabel(prefix + (item.getDefinition() != null ? item.getDefinition().getItemName().getLocalPart() : item.getElementName().getLocalPart()));
        setValue(TraceUtils.prettyPrint(item));

        createChildren();
    }

    private void createChildren() throws SchemaException {
        Item<?, ?> item = getUserObject();

        if (item.getValues().size() > 1) {
            for (int i = 0; i < item.getValues().size(); i++) {
                PrismValueNode.create("#" + i, item.getValues().get(i), this);
            }
        } else if (item.getValues().size() == 1) {
            Node dummyRoot = PrismValueNode.create("dummy", item.getValues().get(0), null);

            Collections.list(dummyRoot.children()).forEach(c -> ItemNode.this.add(c));
        }
    }

    public static ItemNode create(Item<?, ?> item, AbstractMutableTreeTableNode parent) throws SchemaException {
        return create("", item, parent);
    }

    public static ItemNode create(String prefix, Item<?, ?> item, AbstractMutableTreeTableNode parent) throws SchemaException {
        ItemNode node = new ItemNode(prefix, item);
        if (parent != null) {
            parent.add(node);
        }
        return node;
    }
}
