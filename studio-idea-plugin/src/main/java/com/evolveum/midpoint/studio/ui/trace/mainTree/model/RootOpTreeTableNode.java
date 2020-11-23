package com.evolveum.midpoint.studio.ui.trace.mainTree.model;

import com.evolveum.midpoint.schema.traces.OpNode;
import org.jetbrains.annotations.Nullable;

/**
 * Root (invisible) trace tree table node.
 */
public class RootOpTreeTableNode extends AbstractOpTreeTableNode {

    public RootOpTreeTableNode(@Nullable OpNode opNode) {
        super(opNode);
    }
}
