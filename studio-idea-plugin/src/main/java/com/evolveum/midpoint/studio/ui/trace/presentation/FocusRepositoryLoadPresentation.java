package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.operations.FocusRepositoryLoadOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;

import java.awt.*;

/**
 *
 */
public class FocusRepositoryLoadPresentation extends AbstractOpNodePresentation<FocusRepositoryLoadOpNode> {

    public FocusRepositoryLoadPresentation(FocusRepositoryLoadOpNode node) {
        super(node);
    }

    @Override
    public Color getBackgroundColor() {
        if (!node.isDisabled()) {
            return Colors.INPUT_1_COLOR;
        } else {
            return null;
        }
    }
}
