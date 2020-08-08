package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.MappingEvaluationOpNode;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.DeltaSetTripleTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.ItemDeltaItemTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.studio.ui.trace.entry.TextNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MappingEvaluationTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MappingSourceEvaluationTraceType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class MappingEvaluationOverviewProvider implements OverviewProvider<MappingEvaluationOpNode> {

    @Override
    public void provideOverview(MappingEvaluationOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {

        MappingEvaluationTraceType trace = node.getTrace();
        if (trace != null) {
            for (MappingSourceEvaluationTraceType source : trace.getSource()) {
                ItemDeltaItemTypeNode sourceNode = ItemDeltaItemTypeNode
                        .create("Source: " + source.getName(), source.getItemDeltaItem(), root);
                initialState.addExpandedPath(root, sourceNode);
            }

            if (trace.getOutput() != null) {
                int index = root.getChildCount();
                initialState.setSelectedIndex(index);
                DeltaSetTripleTypeNode outputNode = DeltaSetTripleTypeNode.create("Output", trace.getOutput(), root);
                initialState.addExpandedPath(root, outputNode);
            }

            TextNode.create("Kind", trace.getMappingKind(), root);
            TextNode.create("Strength", trace.getMapping() != null ? trace.getMapping().getStrength() : null, root);
            PrismValueNode.create("Mapping", trace.getMapping(), root);
            PrismValueNode.create("Text trace", trace.getTextTrace(), root);
            TextNode.create("Condition", getConditionInfo(trace), root);
            TextNode.create("Time validity", getTimeValidityInfo(trace), root);
            PrismValueNode.create("Containing object", trace.getContainingObjectRef(), root);
            TextNode.create("Context", node.getContext(), root);
        }
    }

    private String getConditionInfo(@NotNull MappingEvaluationTraceType trace) {
        return trace.isConditionResultOld() + " -> " + trace.isConditionResultNew();
    }

    private String getTimeValidityInfo(@NotNull MappingEvaluationTraceType trace) {
        if (Boolean.TRUE.equals(trace.isTimeConstraintValid()) && trace.getNextRecomputeTime() == null) {
            return "OK";
        } else {
            return "Validity: " + trace.isTimeConstraintValid() + ", next recompute: " + trace.getNextRecomputeTime();
        }
    }
}
