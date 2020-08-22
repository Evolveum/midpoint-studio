package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.operations.*;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class OverviewProviderRegistry {

    @SuppressWarnings("unchecked")
    @NotNull
    public static <O extends OpNode> OverviewProvider<O> getProvider(@NotNull O node) {
        if (node instanceof ClockworkRunOpNode) {
            return (OverviewProvider<O>) new ClockworkRunOverviewProvider();
        } else if (node instanceof ClockworkClickOpNode) {
            return (OverviewProvider<O>) new ClockworkClickOverviewProvider();
        } else if (node instanceof ProjectorProjectionOpNode) {
            return (OverviewProvider<O>) new ProjectorProjectionOverviewProvider();
        } else if (node instanceof ProjectorComponentOpNode) {
            return (OverviewProvider<O>) new ProjectorComponentOverviewProvider();
        } else if (node instanceof FocusChangeExecutionOpNode) {
            return (OverviewProvider<O>) new FocusChangeExecutionOverviewProvider();
        } else if (node instanceof MappingEvaluationOpNode) {
            return (OverviewProvider<O>) new MappingEvaluationOverviewProvider();
        } else if (node instanceof TransformationExpressionEvaluationOpNode) {
            return (OverviewProvider<O>) new TransformationExpressionEvaluationOverviewProvider();
        } else if (node instanceof ItemConsolidationOpNode) {
            return (OverviewProvider<O>) new ItemConsolidationOverviewProvider();
        } else {
            return (OverviewProvider<O>) new NullOverviewProvider();
        }
    }
}
