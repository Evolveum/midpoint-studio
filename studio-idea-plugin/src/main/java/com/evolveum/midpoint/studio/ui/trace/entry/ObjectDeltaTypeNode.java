package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ChangeTypeType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

/**
 * Fake class - contains only the code to construct node subtree for given delta.
 *
 * FIXME
 */
public class ObjectDeltaTypeNode extends Node<ObjectDeltaType> {

    public static Node<?> create(String prefix, boolean past, ObjectDeltaType delta, String objectName, AbstractMutableTreeTableNode parent) throws SchemaException {
        if (delta == null) {
            return null;
        }

        ChangeTypeType changeType = delta.getChangeType();
        String typeName = TraceUtils.getTypeName(delta);

        if (changeType == ChangeTypeType.ADD) {
            return ItemNode.create(getLabel(prefix, past, "Add", "Added") + typeName + " ", delta.getObjectToAdd().asPrismObject(), parent);
        } else if (changeType == ChangeTypeType.MODIFY) {
            Node<?> deltaNode = TextNode.create(getLabel(prefix, past, "Modify", "Modified") + typeName + " " + objectName, delta.getItemDelta().size() + " modification(s)", parent);
            for (ItemDeltaType itemDelta : delta.getItemDelta()) {
                ItemDeltaTypeNode.create(String.valueOf(itemDelta.getPath()), itemDelta, false, deltaNode);
            }
            return deltaNode;
        } else if (changeType == ChangeTypeType.DELETE) {
            return TextNode.create(getLabel(prefix, past, "Delete", "Deleted") + typeName + " " + objectName, delta.getOid(), parent);
        } else {
            return null;
        }
    }

    private static String getLabel(String prefix, boolean past, String forPresent, String forPast) {
        return prefix + (past ? forPast : forPresent) + " ";
    }
}
