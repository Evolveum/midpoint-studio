package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public class NullOverviewProvider implements OverviewProvider<OpNode> {

    @Override
    public void provideOverview(OpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) {
        // no op
    }
}
