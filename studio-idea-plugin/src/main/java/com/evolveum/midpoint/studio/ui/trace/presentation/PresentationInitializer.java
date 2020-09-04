package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.operations.*;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    private static AbstractOpNodePresentation<?> createPresentation(OpNode node) {
        if (node instanceof FocusRepositoryLoadOpNode) {
            return new FocusRepositoryLoadPresentation((FocusRepositoryLoadOpNode) node);
        } else if (node instanceof FullProjectionLoadOpNode) {
            return new FullProjectionLoadPresentation((FullProjectionLoadOpNode) node);
        } else if (node instanceof ProjectorProjectionOpNode) {
            return new ProjectorProjectionPresentation((ProjectorProjectionOpNode) node);
        } else if (node instanceof FocusChangeExecutionOpNode) {
            return new FocusChangeExecutionPresentation((FocusChangeExecutionOpNode) node);
        } else if (node instanceof ProjectionChangeExecutionOpNode) {
            return new ProjectionChangeExecutionPresentation((ProjectionChangeExecutionOpNode) node);
        } else if (node instanceof MappingEvaluationOpNode) {
            return new MappingEvaluationPresentation((MappingEvaluationOpNode) node);
        } else {
            return new DefaultOpNodePresentation(node);
        }
    }
}
