package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.MappingEvaluationOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
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
                sourceNode.setBackgroundColor(Colors.INPUT_1_COLOR, true);
            }

            if (trace.getOutput() != null) {
                int index = root.getChildCount();
                initialState.setSelectedIndex(index);
                DeltaSetTripleTypeNode outputNode = DeltaSetTripleTypeNode.create("Output", trace.getOutput(), root);
                initialState.addExpandedPath(root, outputNode);
                outputNode.setBackgroundColor(Colors.OUTPUT_1_COLOR, true);
            }

            TextNode.create("Time validity", getTimeValidityInfo(trace), root)
                    .setBackgroundColor(Colors.OUTPUT_2_COLOR, true);
            TextNode.create("Condition", getConditionInfo(trace), root)
                    .setBackgroundColor(Colors.OUTPUT_3_COLOR, true);

            TextNode.create("Kind", trace.getMappingKind(), root);
            TextNode.create("Strength", trace.getMapping() != null ? trace.getMapping().getStrength() : null, root);
            PrismValueNode.create("Mapping", trace.getMapping(), root)
                    .setBackgroundColor(Colors.CONFIGURATION_1_COLOR, true);
            PrismValueNode.create("Text trace", trace.getTextTrace(), root);
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
            StringBuilder sb = new StringBuilder();
            if (trace.isTimeConstraintValid() == null) {
                sb.append("Validity unknown. ");
            } else if (trace.isTimeConstraintValid()) {
                sb.append("Valid. ");
            } else {
                sb.append("Invalid. ");
            }
            if (trace.getNextRecomputeTime() != null) {
                sb.append("Next recompute: ").append(trace.getNextRecomputeTime());
            } else {
                sb.append("No next recompute.");
            }
            return sb.toString();
        }
    }
}
