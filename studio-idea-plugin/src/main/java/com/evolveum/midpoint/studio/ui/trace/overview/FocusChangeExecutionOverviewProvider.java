package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.traces.operations.FocusChangeExecutionOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.*;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectDeltaOperationType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public class FocusChangeExecutionOverviewProvider implements OverviewProvider<FocusChangeExecutionOpNode> {

    @Override
    public void provideOverview(FocusChangeExecutionOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {
        ObjectDeltaOperationType odo = node.getObjectDeltaOperation();
        if (odo != null) {
            ObjectDeltaType delta = odo.getObjectDelta();
            String typeName = TraceUtils.getTypeName(delta);

            TextNode.create("Object", odo.getObjectName() + " (" + typeName + ")", root);
            if (delta != null) {
                Node<?> deltaNode = ObjectDeltaTypeNode.create("", true, delta, PolyString.getOrig(odo.getObjectName()), root);
                if (deltaNode != null) {
                    initialState.setSelectedIndex(2);
                    initialState.addExpandedPath(root, deltaNode);
                    deltaNode.setBackgroundColor(Colors.INPUT_1_COLOR, true);
                }
            }
            if (odo.getExecutionResult() != null) {
                TextNode.create("Result", odo.getExecutionResult().getStatus(), root);
            }
        }
    }
}
