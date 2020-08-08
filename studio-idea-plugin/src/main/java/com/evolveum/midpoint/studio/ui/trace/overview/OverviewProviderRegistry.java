package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.operations.FocusChangeExecutionOpNode;
import com.evolveum.midpoint.schema.traces.operations.ItemConsolidationOpNode;
import com.evolveum.midpoint.schema.traces.operations.MappingEvaluationOpNode;
import com.evolveum.midpoint.schema.traces.operations.TransformationExpressionEvaluationOpNode;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class OverviewProviderRegistry {

    @SuppressWarnings("unchecked")
    @NotNull
    public static <O extends OpNode> OverviewProvider<O> getProvider(@NotNull O node) {
        if (node instanceof FocusChangeExecutionOpNode) {
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
