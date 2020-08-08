package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class PrismValueNode extends Node<PrismValue> {

    public PrismValueNode(String label, PrismValue prismValue) throws SchemaException {
        super(prismValue);

        setLabel(label);
        setValue(TraceUtils.prettyPrint(prismValue));

        createChildren();
    }

    private void createChildren() throws SchemaException {
        PrismValue prismValue = getUserObject();

        if (!(prismValue instanceof PrismContainerValue)) {
            return;
        }

        PrismContainerValue<?> pcv = (PrismContainerValue<?>) prismValue;
        List<ItemName> itemNames = getItemNames();
        for (ItemName itemName : itemNames) {
            Item<?, ?> item = pcv.findItem(itemName);
            if (item != null) {
                ItemNode.create(item, this);
            }
        }
    }

    private List<ItemName> getItemNames() throws SchemaException {
        PrismValue prismValue = getUserObject();

        List<ItemName> rv = new ArrayList<>();
        PrismContainerValue<?> pcv = (PrismContainerValue<?>) prismValue;
        ComplexTypeDefinition ctd = pcv.getComplexTypeDefinition();
        if (ctd != null) {
            @SuppressWarnings("rawtypes")
            List<? extends ItemDefinition> definitions = ctd.getDefinitions();
            definitions.stream().map(def -> def.getItemName()).forEach(name -> rv.add(name));
        }
        for (QName name : pcv.getItemNames()) {
            ItemName itemName = ItemName.fromQName(name);
            if (!rv.contains(itemName)) {
                rv.add(itemName);
            }
        }
        return rv;
    }

    public static PrismValueNode create(String string, Object realValue, AbstractMutableTreeTableNode parent) throws SchemaException {
        if (realValue instanceof Containerable) {
            return create(string, ((Containerable) realValue).asPrismContainerValue(), parent);
        } else if (realValue instanceof Referencable) {
            Referencable referencable = (Referencable) realValue;
            return create(string, referencable.asReferenceValue(), parent);
        } else if (realValue != null) {
            ItemFactory itemFactory = MidPointUtils.DEFAULT_PRISM_CONTEXT.itemFactory();
            return create(string, itemFactory.createPropertyValue(realValue), parent);
        } else {
            return create(string, null, parent);
        }
    }

    public static PrismValueNode create(String string, PrismValue value, AbstractMutableTreeTableNode parent) throws SchemaException {
        PrismValueNode node = new PrismValueNode(string, value);
        if (parent != null) {
            parent.add(node);
        }
        return node;
    }
}
