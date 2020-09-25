package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * "Regular" (potentially visible) trace tree table nodes.
 * User object i.e. OpNode is always non-null here.
 */
public class RegularTraceTreeTableNode extends AbstractTraceTreeTableNode {

    public RegularTraceTreeTableNode(@NotNull OpNode opNode) {
        super(opNode);
    }

    @NotNull
    @Override
    public OpNode getUserObject() {
        return Objects.requireNonNull(super.getUserObject());
    }
}
