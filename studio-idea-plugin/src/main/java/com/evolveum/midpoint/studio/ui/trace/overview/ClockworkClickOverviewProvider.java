package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.ClockworkClickOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.Node;
import com.evolveum.midpoint.studio.ui.trace.entry.ObjectDeltaTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ClockworkClickTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensFocusContextType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import static com.evolveum.midpoint.studio.ui.trace.TraceUtils.getObjectFromReference;

/**
 *
 */
public class ClockworkClickOverviewProvider implements OverviewProvider<ClockworkClickOpNode> {

    @Override
    public void provideOverview(ClockworkClickOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {

        ClockworkClickTraceType trace = node.getTrace();
        if (trace != null) {
            LensContextType inputContext = trace.getInputLensContext();
            if (inputContext != null) {
                root.add(new PrismValueNode("Context (before)", inputContext.asPrismContainerValue()));
                LensFocusContextType focusContext = inputContext.getFocusContext();
                if (focusContext != null) {
                    PrismValueNode.create("Focus old", getObjectFromReference(focusContext.getObjectOldRef()), root);
                    PrismValueNode.create("Focus current", getObjectFromReference(focusContext.getObjectCurrentRef()), root)
                            .setBackgroundColor(Colors.INPUT_1_COLOR, true);

                    ObjectDeltaType primaryDelta = focusContext.getPrimaryDelta();
                    Node<?> deltaNode = ObjectDeltaTypeNode.create("Focus primary delta: ", false, primaryDelta, node.getFocusName(), root);
                    if (deltaNode != null) {
                        deltaNode.setBackgroundColor(Colors.INPUT_2_COLOR, true);
                    }
                }
            }

            LensContextType outputContext = trace.getOutputLensContext();
            if (outputContext != null) {
                LensFocusContextType focusContext = outputContext.getFocusContext();
                if (focusContext != null) {
                    ObjectDeltaType secondaryDelta = focusContext.getSecondaryDelta();
                    Node<?> deltaNode = ObjectDeltaTypeNode.create("Focus secondary delta (after): ", false, secondaryDelta, node.getFocusName(), root);
                    if (deltaNode != null) {
                        deltaNode.setBackgroundColor(Colors.OUTPUT_2_COLOR, true);
                    }
                    PrismValueNode.create("Focus new", getObjectFromReference(focusContext.getObjectNewRef()), root)
                            .setBackgroundColor(Colors.OUTPUT_1_COLOR, true);
                }
                root.add(new PrismValueNode("Context (after)", outputContext.asPrismContainerValue()));
            }
        }
    }
}
