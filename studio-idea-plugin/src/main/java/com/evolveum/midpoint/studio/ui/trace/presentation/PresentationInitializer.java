package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.RepositoryCacheOpNode;
import com.evolveum.midpoint.schema.traces.operations.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 */
public class PresentationInitializer {

    private static final List<PresentationSupplier<?>> SUPPLIERS = new ArrayList<>();

    static {
        SUPPLIERS.add(new PresentationSupplier<>(RepositoryCacheOpNode.class, RepositoryCacheOperationPresentation::new));
        SUPPLIERS.add(new PresentationSupplier<>(FocusRepositoryLoadOpNode.class, FocusRepositoryLoadPresentation::new));
        SUPPLIERS.add(new PresentationSupplier<>(FullProjectionLoadOpNode.class, FullProjectionLoadPresentation::new));
        SUPPLIERS.add(new PresentationSupplier<>(ProjectorProjectionOpNode.class, ProjectorProjectionPresentation::new));
        SUPPLIERS.add(new PresentationSupplier<>(FocusChangeExecutionOpNode.class, FocusChangeExecutionPresentation::new));
        SUPPLIERS.add(new PresentationSupplier<>(ProjectionChangeExecutionOpNode.class, ProjectionChangeExecutionPresentation::new));
        SUPPLIERS.add(new PresentationSupplier<>(MappingEvaluationOpNode.class, MappingEvaluationPresentation::new));
    }

    public static void initialize(OpNode node) {
        initializeForNode(node);
        node.getChildren().forEach(PresentationInitializer::initialize);
    }

    private static void initializeForNode(OpNode node) {
        node.setPresentation(createPresentation(node));
    }

    @NotNull
    private static AbstractOpNodePresentation<?> createPresentation(OpNode node) {
        for (PresentationSupplier<?> supplier : SUPPLIERS) {
            AbstractOpNodePresentation<?> presentation = supplier.create(node);
            if (presentation != null) {
                return presentation;
            }
        }
        return new DefaultOpNodePresentation(node);
    }

    private static class PresentationSupplier<X extends OpNode> {
        private final Class<X> opNodeType;
        private final Function<X, AbstractOpNodePresentation<X>> creator;

        private PresentationSupplier(Class<X> opNodeType, Function<X, AbstractOpNodePresentation<X>> creator) {
            this.opNodeType = opNodeType;
            this.creator = creator;
        }

        AbstractOpNodePresentation<?> create(OpNode opNode) {
            if (opNodeType.isAssignableFrom(opNode.getClass())) {
                //noinspection unchecked
                return creator.apply((X) opNode);
            } else {
                return null;
            }
        }
    }
}
