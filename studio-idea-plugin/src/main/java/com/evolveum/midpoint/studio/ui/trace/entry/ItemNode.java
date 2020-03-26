package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.studio.ui.trace.lens.Util;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.apache.commons.lang.Validate;

import java.util.Collections;

public class ItemNode extends Node {

    private final Item<?, ?> item;

    public ItemNode(Item<?, ?> item) throws SchemaException {
        Validate.notNull(item);

        this.item = item;

        createChildren();
    }

    @Override
    public String getLabel() {
        return item.getDefinition() != null ? item.getDefinition().getItemName().getLocalPart() : item.getElementName().getLocalPart();
    }

    @Override
    public String getValue() {
        return Util.prettyPrint(item);
    }

    private void createChildren() throws SchemaException {
        if (item.getValues().size() > 1) {
            for (int i = 0; i < item.getValues().size(); i++) {
                int index = i;
                PrismValueNode.create("#" + i, item.getValues().get(i), this);
            }
        } else if (item.getValues().size() == 1) {
            Node dummyRoot = PrismValueNode.create("dummy", item.getValues().get(0), null);

            Collections.list(dummyRoot.children()).forEach(c -> ItemNode.this.add(c));
        }
    }

    public static ItemNode create(Item<?, ?> item, Node parent) throws SchemaException {
        ItemNode node = new ItemNode(item);
        if (parent != null) {
            parent.add(node);
        }

        return node;
    }

    @Override
    public Object getObject() {
        return item;
    }
}
