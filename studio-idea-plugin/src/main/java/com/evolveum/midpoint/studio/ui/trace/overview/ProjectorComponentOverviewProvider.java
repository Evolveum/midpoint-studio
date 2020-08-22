package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.ProjectorComponentOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.Node;
import com.evolveum.midpoint.studio.ui.trace.entry.ObjectDeltaTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensFocusContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ProjectorComponentTraceType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public class ProjectorComponentOverviewProvider implements OverviewProvider<ProjectorComponentOpNode> {

    @Override
    public void provideOverview(ProjectorComponentOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {

        ProjectorComponentTraceType trace = node.getTrace();
        if (trace != null) {
            LensContextType inputContext = trace.getInputLensContext();
            if (inputContext != null) {
                LensFocusContextType focusContext = inputContext.getFocusContext();
                if (focusContext != null) {
                    PrismValueNode.create("Focus old", focusContext.getObjectOld(), root);
                    PrismValueNode.create("Focus current", focusContext.getObjectCurrent(), root)
                            .setBackgroundColor(Colors.INPUT_1_COLOR, true);

                    ObjectDeltaType primaryDelta = focusContext.getPrimaryDelta();
                    Node<?> deltaNode = ObjectDeltaTypeNode.create("Primary delta: ", false, primaryDelta, node.getFocusName(), root);
                    if (deltaNode != null) {
                        deltaNode.setBackgroundColor(Colors.INPUT_2_COLOR, true);
                    }

                    ObjectDeltaType secondaryDelta = focusContext.getSecondaryDelta();
                    Node<?> secDeltaNode = ObjectDeltaTypeNode.create("Secondary delta (before): ", false, secondaryDelta, node.getFocusName(), root);
                    if (secDeltaNode != null) {
                        secDeltaNode.setBackgroundColor(Colors.INPUT_1_COLOR, true);
                    }
                }
            }

            LensContextType outputContext = trace.getOutputLensContext();
            if (outputContext != null) {
                LensFocusContextType focusContext = outputContext.getFocusContext();
                if (focusContext != null) {
                    ObjectDeltaType secondaryDelta = focusContext.getSecondaryDelta();
                    Node<?> deltaNode = ObjectDeltaTypeNode.create("Secondary delta (after): ", false, secondaryDelta, node.getFocusName(), root);
                    if (deltaNode != null) {
                        deltaNode.setBackgroundColor(Colors.OUTPUT_2_COLOR, true);
                    }
                    PrismValueNode.create("Focus new", focusContext.getObjectNew(), root)
                            .setBackgroundColor(Colors.OUTPUT_1_COLOR, true);
                }
            }
        }
    }
}
