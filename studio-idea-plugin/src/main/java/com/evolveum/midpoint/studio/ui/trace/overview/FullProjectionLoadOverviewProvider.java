package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.FullProjectionLoadOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.studio.ui.trace.entry.TextNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FullShadowLoadedTraceType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public class FullProjectionLoadOverviewProvider implements OverviewProvider<FullProjectionLoadOpNode> {

    @Override
    public void provideOverview(FullProjectionLoadOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {

        FullShadowLoadedTraceType trace = node.getTrace(FullShadowLoadedTraceType.class);
        if (trace != null) {
            TextNode.create("Resource", trace.getResourceName(), root);
            TextNode.create("Reason", trace.getReason(), root)
                    .setBackgroundColor(Colors.INPUT_1_COLOR, true);
            if (trace.getShadowLoadedRef() != null) {
                PrismValueNode.create("Shadow loaded", trace.getShadowLoadedRef().asReferenceValue().getObject(), root)
                        .setBackgroundColor(Colors.OUTPUT_1_COLOR, true);
            }
        }
    }
}
