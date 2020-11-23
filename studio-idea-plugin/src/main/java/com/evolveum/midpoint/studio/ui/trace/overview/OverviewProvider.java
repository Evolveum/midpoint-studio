package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public interface OverviewProvider<O extends OpNode> {

    void provideOverview(O node, DefaultMutableTreeTableNode root, ViewingState initialState) throws SchemaException;
}
