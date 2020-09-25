package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.RepositoryCacheOpNode;
import com.evolveum.midpoint.schema.traces.operations.*;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 */
public class OverviewProviderRegistry {

    private static final Map<Class<? extends OpNode>, Supplier<OverviewProvider<?>>> PROVIDERS = new LinkedHashMap<>();

    static {
        PROVIDERS.put(RepositoryCacheOpNode.class, RepositoryCacheOperationOverviewProvider::new);
        PROVIDERS.put(FocusRepositoryLoadOpNode.class, FocusRepositoryLoadOverviewProvider::new);
        PROVIDERS.put(FullProjectionLoadOpNode.class, FullProjectionLoadOverviewProvider::new);
        PROVIDERS.put(ClockworkRunOpNode.class, ClockworkRunOverviewProvider::new);
        PROVIDERS.put(ClockworkClickOpNode.class, ClockworkClickOverviewProvider::new);
        PROVIDERS.put(ProjectorProjectionOpNode.class, ProjectorProjectionOverviewProvider::new);
        PROVIDERS.put(ProjectorComponentOpNode.class, ProjectorComponentOverviewProvider::new);
        PROVIDERS.put(FocusChangeExecutionOpNode.class, FocusChangeExecutionOverviewProvider::new);
        PROVIDERS.put(ProjectionChangeExecutionOpNode.class, ProjectionChangeExecutionOverviewProvider::new);
        PROVIDERS.put(ResourceObjectConstructionEvaluationOpNode.class, ResourceObjectConstructionEvaluationOverviewProvider::new);
        PROVIDERS.put(MappingEvaluationOpNode.class, MappingEvaluationOverviewProvider::new);
        PROVIDERS.put(TransformationExpressionEvaluationOpNode.class, TransformationExpressionEvaluationOverviewProvider::new);
        PROVIDERS.put(ItemConsolidationOpNode.class, ItemConsolidationOverviewProvider::new);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <O extends OpNode> OverviewProvider<O> getProvider(@NotNull O node) {
        for (Map.Entry<Class<? extends OpNode>, Supplier<OverviewProvider<?>>> entry : PROVIDERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(node.getClass())) {
                return (OverviewProvider<O>) entry.getValue().get();
            }
        }
        return (OverviewProvider<O>) new NullOverviewProvider();
    }
}
