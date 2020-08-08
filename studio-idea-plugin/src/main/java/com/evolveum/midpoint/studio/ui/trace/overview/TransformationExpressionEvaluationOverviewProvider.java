package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.TransformationExpressionEvaluationOpNode;
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
                initialState.addExpandedPath(root, sourceNode);
            }
            for (ExpressionSourceEvaluationTraceType source : trace.getSource()) {
                if (source.getDeltaSetTriple() != null) {
                    DeltaSetTripleTypeNode sourceNode = DeltaSetTripleTypeNode
                            .create("Source: " + source.getName() + " triple", source.getDeltaSetTriple(), root);
                    initialState.addExpandedPath(root, sourceNode);
                }
            }

            if (trace.getOutput() != null) {
                int index = root.getChildCount();
                initialState.setSelectedIndex(index);
                DeltaSetTripleTypeNode outputNode = DeltaSetTripleTypeNode.create("Output", trace.getOutput(), root);
                initialState.addExpandedPath(root, outputNode);
            }

            TextNode.create("Evaluation mode", trace.getEvaluationMode(), root);
            PrismValueNode.create("Evaluator", trace.getExpressionEvaluator(), root);
            TextNode.create("Skipped evaluation", getSkippedEvaluation(trace), root);
            TextNode.create("Context", trace.getContextDescription(), root);
        }
    }

    private String getSkippedEvaluation(ValueTransformationExpressionEvaluationTraceType trace) {
        return (Boolean.TRUE.equals(trace.isSkipEvaluationPlus()) ? "PLUS " : "") +
                (Boolean.TRUE.equals(trace.isSkipEvaluationMinus()) ? "MINUS" : "");
    }
}
