package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.DeltaSetTripleType;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import java.util.List;

public class DeltaSetTripleTypeNode extends Node<DeltaSetTripleType> {

    public DeltaSetTripleTypeNode(String label, DeltaSetTripleType triple) throws SchemaException {
        super(triple);

        setLabel(label);
        setValue(TraceUtils.prettyPrint(triple));

        createChildren();
    }

    private void createChildren() throws SchemaException {
        DeltaSetTripleType triple = getUserObject();

        if (triple != null) {
            createNodesForValues(triple.getPlus(), "Plus");
            createNodesForValues(triple.getMinus(), "Minus");
            createNodesForValues(triple.getZero(), "Zero");
        }
    }

    private void createNodesForValues(List<Object> values, String label) throws SchemaException {
        for (Object value : values) {
            PrismValueNode.create(label, value, this);
        }
    }

    public static DeltaSetTripleTypeNode create(String label, DeltaSetTripleType value, AbstractMutableTreeTableNode parent) throws SchemaException {
        DeltaSetTripleTypeNode node = new DeltaSetTripleTypeNode(label, value);
        if (parent != null) {
            parent.add(node);
        }
        return node;
    }
}
