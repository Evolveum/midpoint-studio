package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.FocusRepositoryLoadOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.ObjectDeltaTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusLoadedTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensFocusContextType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import static com.evolveum.midpoint.studio.ui.trace.TraceUtils.getObjectFromReference;

/**
 *
 */
public class FocusRepositoryLoadOverviewProvider implements OverviewProvider<FocusRepositoryLoadOpNode> {

    @Override
    public void provideOverview(FocusRepositoryLoadOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {

        FocusLoadedTraceType trace = node.getTrace(FocusLoadedTraceType.class);
        if (trace != null) {
            LensContextType inputContext = trace.getInputLensContext();
            if (inputContext != null) {
                LensFocusContextType focusContext = inputContext.getFocusContext();
                if (focusContext != null) {
                    PrismValueNode.create("Focus old", getObjectFromReference(focusContext.getObjectOldRef()), root);

                    ObjectDeltaType primaryDelta = focusContext.getPrimaryDelta();
                    ObjectDeltaTypeNode.create("Primary delta: ", false, primaryDelta, node.getFocusName(), root);

                    ObjectDeltaType secondaryDelta = focusContext.getSecondaryDelta();
                    ObjectDeltaTypeNode.create("Secondary delta (before): ", false, secondaryDelta, node.getFocusName(), root);
                }
            }

            LensContextType outputContext = trace.getOutputLensContext();
            if (outputContext != null) {
                LensFocusContextType focusContext = outputContext.getFocusContext();
                if (focusContext != null) {
                    PrismValueNode.create("Focus current", getObjectFromReference(focusContext.getObjectCurrentRef()), root)
                            .setBackgroundColor(Colors.OUTPUT_1_COLOR, true);
                    PrismValueNode.create("Focus new", getObjectFromReference(focusContext.getObjectNewRef()), root)
                            .setBackgroundColor(Colors.OUTPUT_2_COLOR_WEAK, true);
                }
            }
        }
    }
}
