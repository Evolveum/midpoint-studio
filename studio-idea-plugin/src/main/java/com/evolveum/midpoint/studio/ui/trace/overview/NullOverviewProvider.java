package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
public class NullOverviewProvider implements OverviewProvider<OpNode> {

    @Override
    public void provideOverview(OpNode node, DefaultMutableTreeNode root,
            ViewingState initialState) {
        // no op
    }
}
