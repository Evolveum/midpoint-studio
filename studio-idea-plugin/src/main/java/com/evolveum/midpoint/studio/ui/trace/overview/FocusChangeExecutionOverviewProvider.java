package com.evolveum.midpoint.studio.ui.trace.overview;

import com.evolveum.midpoint.schema.traces.operations.FocusChangeExecutionOpNode;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.ItemDeltaTypeNode;
import com.evolveum.midpoint.studio.ui.trace.entry.ItemNode;
import com.evolveum.midpoint.studio.ui.trace.entry.Node;
import com.evolveum.midpoint.studio.ui.trace.entry.TextNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectDeltaOperationType;
import com.evolveum.prism.xml.ns._public.types_3.ChangeTypeType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 */
public class FocusChangeExecutionOverviewProvider implements OverviewProvider<FocusChangeExecutionOpNode> {

    @Override
    public void provideOverview(FocusChangeExecutionOpNode node, DefaultMutableTreeTableNode root,
            ViewingState initialState) throws SchemaException {
        ObjectDeltaOperationType odo = node.getObjectDeltaOperation();
        if (odo != null) {
            ObjectDeltaType delta = odo.getObjectDelta();
            String typeName = delta != null && delta.getObjectType() != null ? delta.getObjectType().getLocalPart() : "object";

            TextNode.create("Object", odo.getObjectName() + " (" + typeName + ")", root);
            if (delta != null) {
                ChangeTypeType changeType = delta.getChangeType();
                Node deltaNode;
                if (changeType == ChangeTypeType.ADD) {
                    deltaNode = ItemNode.create("Added " + typeName + " ", delta.getObjectToAdd().asPrismObject(), root);
                } else if (changeType == ChangeTypeType.MODIFY) {
                    deltaNode = TextNode.create("Modified " + typeName + " " + odo.getObjectName(), delta.getItemDelta().size() + " modification(s)", root);
                    for (ItemDeltaType itemDelta : delta.getItemDelta()) {
                        ItemDeltaTypeNode.create(String.valueOf(itemDelta.getPath()), itemDelta, false, deltaNode);
                    }
                } else if (changeType == ChangeTypeType.DELETE) {
                    deltaNode = TextNode.create("Deleted " + typeName + " " + odo.getObjectName(), delta.getOid(), root);
                } else {
                    deltaNode = null;
                }
                initialState.setSelectedIndex(2);
                initialState.addExpandedPath(root, deltaNode);
            }
            if (odo.getExecutionResult() != null) {
                TextNode.create("Result", odo.getExecutionResult().getStatus(), root);
            }
        }
    }
}
