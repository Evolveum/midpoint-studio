package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.operations.MappingEvaluationOpNode;

/**
 *
 */
public class MappingEvaluationPresentation extends AbstractOpNodePresentation<MappingEvaluationOpNode> {

    public MappingEvaluationPresentation(MappingEvaluationOpNode node) {
        super(node);
    }

    @Override
    public String getLabel() {
        return "Mapping: " + node.getMappingInfo();
    }
}
