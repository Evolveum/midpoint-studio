package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.operations.FocusChangeExecutionOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;

import java.awt.*;

/**
 *
 */
public class FocusChangeExecutionPresentation extends AbstractOpNodePresentation<FocusChangeExecutionOpNode> {

    public FocusChangeExecutionPresentation(FocusChangeExecutionOpNode node) {
        super(node);
    }

    @Override
    public Color getBackgroundColor() {
        if (!node.isDisabled()) {
            return Colors.OUTPUT_1_COLOR;
        } else {
            return null;
        }
    }
}
