package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.operations.FullProjectionLoadOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;

import java.awt.*;

/**
 *
 */
public class FullProjectionLoadPresentation extends AbstractOpNodePresentation<FullProjectionLoadOpNode> {

    public FullProjectionLoadPresentation(FullProjectionLoadOpNode node) {
        super(node);
    }

    @Override
    public Color getBackgroundColor() {
        if (!node.isDisabled()) {
            return Colors.INPUT_2_COLOR;
        } else {
            return null;
        }
    }
}
