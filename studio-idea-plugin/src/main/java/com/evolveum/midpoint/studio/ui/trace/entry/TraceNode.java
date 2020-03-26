package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ModelExecuteDeltaTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;

public class TraceNode<T extends TraceType> extends Node {

    protected final T trace;

    public TraceNode(T trace) {
        this.trace = trace;
    }

    @Override
    public String getLabel() {
        return trace.getClass().getSimpleName();
    }

    @Override
    public String getValue() {
        return "...";
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TraceNode;            // to avoid closing&reopening of this node in the tree
        // TODO
    }

    public static Node create(TraceType trace, TextNode parent) throws SchemaException {
        Node node;
        if (trace instanceof ModelExecuteDeltaTraceType) {
            node = new ModelExecuteDeltaTraceNode((ModelExecuteDeltaTraceType) trace);
            if (parent != null) {
                parent.add(node);
            }
        } else if (trace != null) {
            node = PrismValueNode.create("Trace", trace.asPrismContainerValue(), parent);
        } else {
            node = TextNode.create("Trace", "", parent);
        }

        return node;
    }

    @Override
    public Object getObject() {
        return trace.asPrismContainerValue();
    }

}
