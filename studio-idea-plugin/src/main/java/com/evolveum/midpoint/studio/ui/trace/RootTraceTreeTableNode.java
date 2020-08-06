package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import org.jetbrains.annotations.Nullable;

/**
 * Root (invisible) trace tree table node.
 */
public class RootTraceTreeTableNode extends AbstractTraceTreeTableNode {

    public RootTraceTreeTableNode(@Nullable OpNode opNode) {
        super(opNode);
    }
}
