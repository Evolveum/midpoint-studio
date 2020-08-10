package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.ItemConsolidationOpNode;
import com.evolveum.midpoint.studio.ui.trace.Colors;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.*;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ItemConsolidationTraceType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public class ItemConsolidationOverviewProvider implements OverviewProvider<ItemConsolidationOpNode> {

    @Override
    public void provideOverview(ItemConsolidationOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {

        ItemConsolidationTraceType trace = node.getTrace();
        if (trace != null) {
            DeltaSetTripleTypeNode deltaSetTripleNode = DeltaSetTripleTypeNode.create("Delta set triple", trace.getDeltaSetTriple(), root);
            deltaSetTripleNode.setBackgroundColor(Colors.INPUT_1_COLOR, true);
            initialState.addExpandedPath(root, deltaSetTripleNode);

            PrismValueNode existingItemNode = PrismValueNode.create("Existing item", trace.getExistingItem(), root); // todo change to item
            existingItemNode.setBackgroundColor(Colors.INPUT_2_COLOR, true);

            ItemDeltaTypeListNode aprioriDeltaNode = ItemDeltaTypeListNode.create("A priori delta", trace.getAprioriDelta(), root);
            aprioriDeltaNode.setBackgroundColor(Colors.INPUT_3_COLOR, true);

            TextNode.create("Equivalence class number", trace.getEquivalenceClassCount(), root);

            if (!trace.getResultingDelta().isEmpty()) {
                int index = root.getChildCount();
                initialState.setSelectedIndex(index);
                ItemDeltaTypeNode resultingDeltaNode = ItemDeltaTypeNode
                        .create("Resulting delta", trace.getResultingDelta().get(0), false, root);
                initialState.addExpandedPath(root, resultingDeltaNode);
                resultingDeltaNode.setBackgroundColor(Colors.OUTPUT_1_COLOR, true);

                for (int i = 1; i < trace.getResultingDelta().size(); i++) {
                    ItemDeltaTypeNode resultingDeltaNodeNext = ItemDeltaTypeNode
                            .create("Resulting delta (" + (i+1) + ")", trace.getResultingDelta().get(i), false, root);
                    initialState.addExpandedPath(root, resultingDeltaNodeNext);
                    resultingDeltaNodeNext.setBackgroundColor(Colors.OUTPUT_1_COLOR, true);
                }
            }
        }
    }
}
