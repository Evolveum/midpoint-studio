package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.operations.ProjectorProjectionOpNode;

import static com.evolveum.midpoint.schema.traces.TraceUtil.getParameter;

/**
 *
 */
public class ProjectorProjectionPresentation extends AbstractOpNodePresentation<ProjectorProjectionOpNode> {

    public ProjectorProjectionPresentation(ProjectorProjectionOpNode node) {
        super(node);
    }

    @Override
    public String getLabel() {
        return "Projector projection: " + getParameter(node.getResult(), "resourceName");
    }
}
