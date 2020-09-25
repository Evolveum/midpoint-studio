package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.TransformationExpressionEvaluationOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.DeltaSetTripleTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.ItemDeltaItemTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.PrismValueNode;
import com.evolveum.midpoint.studio.ui.trace.entry.TextNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ExpressionSourceEvaluationTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValueTransformationExpressionEvaluationTraceType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public class TransformationExpressionEvaluationOverviewProvider implements OverviewProvider<TransformationExpressionEvaluationOpNode> {

    @Override
    public void provideOverview(TransformationExpressionEvaluationOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {

        ValueTransformationExpressionEvaluationTraceType trace = node.getTrace();
        if (trace != null) {
            for (ExpressionSourceEvaluationTraceType source : trace.getSource()) {
                ItemDeltaItemTypeNode sourceNode = ItemDeltaItemTypeNode.create("Source: " + source.getName() + " IDI", source.getItemDeltaItem(), root);
                sourceNode.setBackgroundColor(Colors.INPUT_1_COLOR, true);
                initialState.addExpandedPath(root, sourceNode);
            }
            for (ExpressionSourceEvaluationTraceType source : trace.getSource()) {
                if (source.getDeltaSetTriple() != null) {
                    DeltaSetTripleTypeNode sourceNode = DeltaSetTripleTypeNode
                            .create("Source: " + source.getName() + " triple", source.getDeltaSetTriple(), root);
                    sourceNode.setBackgroundColor(Colors.INPUT_2_COLOR, true);
                    initialState.addExpandedPath(root, sourceNode);
                }
            }

            if (trace.getOutput() != null) {
                int index = root.getChildCount();
                initialState.setSelectedIndex(index);
                DeltaSetTripleTypeNode outputNode = DeltaSetTripleTypeNode.create("Output", trace.getOutput(), root);
                outputNode.setBackgroundColor(Colors.OUTPUT_1_COLOR, true);
                initialState.addExpandedPath(root, outputNode);
            }

            TextNode.create("Evaluation mode", trace.getEvaluationMode(), root);
            PrismValueNode.create("Evaluator", trace.getExpressionEvaluator(), root)
                    .setBackgroundColor(Colors.CONFIGURATION_1_COLOR, true);
            TextNode.create("Skipped evaluation", getSkippedEvaluation(trace), root);
            TextNode.create("Context", trace.getContextDescription(), root);
        }
    }

    private String getSkippedEvaluation(ValueTransformationExpressionEvaluationTraceType trace) {
        return (Boolean.TRUE.equals(trace.isSkipEvaluationPlus()) ? "PLUS " : "") +
                (Boolean.TRUE.equals(trace.isSkipEvaluationMinus()) ? "MINUS" : "");
    }
}
