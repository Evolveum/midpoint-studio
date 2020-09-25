package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.operations.ProjectionChangeExecutionOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;

import java.awt.*;

/**
 *
 */
public class ProjectionChangeExecutionPresentation extends AbstractOpNodePresentation<ProjectionChangeExecutionOpNode> {

    public ProjectionChangeExecutionPresentation(ProjectionChangeExecutionOpNode node) {
        super(node);
    }

    @Override
    public Color getBackgroundColor() {
        if (node.isDisabled()) {
            return null;
        } else if (node.hasDelta()) {
            return Colors.OUTPUT_2_COLOR;
        } else {
            return Colors.OUTPUT_2_COLOR_WEAK;
        }
    }
}
