package com.evolveum.midpoint.studio.ui.trace.mainTree.model;

import com.evolveum.midpoint.schema.traces.OpNode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * "Regular" (potentially visible) trace tree table nodes.
 * User object i.e. OpNode is always non-null here.
 */
public class RegularOpTreeTableNode extends AbstractOpTreeTableNode {

    public RegularOpTreeTableNode(@NotNull OpNode opNode) {
        super(opNode);
    }

    @NotNull
    @Override
    public OpNode getUserObject() {
        return Objects.requireNonNull(super.getUserObject());
    }
}
