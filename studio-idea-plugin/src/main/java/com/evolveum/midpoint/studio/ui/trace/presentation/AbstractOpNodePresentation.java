package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.OpNodePresentation;
import com.evolveum.midpoint.studio.ui.trace.overview.OverviewProvider;
import com.evolveum.midpoint.studio.ui.trace.overview.OverviewProviderRegistry;

/**
 *
 */
abstract public class AbstractOpNodePresentation<O extends OpNode> implements OpNodePresentation {

    final O node;
    private final OverviewProvider<O> overviewProvider;

    protected AbstractOpNodePresentation(O node) {
        this.node = node;
        this.overviewProvider = OverviewProviderRegistry.getProvider(node);
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getToolTip() {
        return null;
    }

    public O getNode() {
        return node;
    }

    public OverviewProvider<O> getOverviewProvider() {
        return overviewProvider;
    }
}
