package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.operations.FocusChangeExecutionOpNode;
import com.evolveum.midpoint.schema.traces.operations.MappingEvaluationOpNode;
import com.evolveum.midpoint.schema.traces.operations.ProjectorProjectionOpNode;

/**
 *
 */
public class PresentationInitializer {

    public static void initialize(OpNode node) {
        initializeForNode(node);
        node.getChildren().forEach(PresentationInitializer::initialize);
    }

    private static void initializeForNode(OpNode node) {
        node.setPresentation(createPresentation(node));
    }

    private static AbstractOpNodePresentation<?> createPresentation(OpNode node) {
        if (node instanceof ProjectorProjectionOpNode) {
            return new ProjectorProjectionPresentation((ProjectorProjectionOpNode) node);
        } else if (node instanceof FocusChangeExecutionOpNode) {
            return new FocusChangeExecutionPresentation((FocusChangeExecutionOpNode) node);
        } else if (node instanceof MappingEvaluationOpNode) {
            return new MappingEvaluationPresentation((MappingEvaluationOpNode) node);
        } else {
            return new DefaultOpNodePresentation(node);
        }
    }
}
