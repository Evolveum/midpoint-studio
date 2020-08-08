package com.evolveum.midpoint.studio.ui.trace.presentation;

import com.evolveum.midpoint.schema.traces.operations.FocusChangeExecutionOpNode;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectDeltaOperationType;
import com.evolveum.prism.xml.ns._public.types_3.ChangeTypeType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;

/**
 *
 */
public class FocusChangeExecutionPresentation extends AbstractOpNodePresentation<FocusChangeExecutionOpNode> {

    public FocusChangeExecutionPresentation(FocusChangeExecutionOpNode node) {
        super(node);
    }

    @Override
    public String getLabel() {
        ObjectDeltaOperationType objectDeltaOperation = node.getObjectDeltaOperation();
        ObjectDeltaType objectDelta = objectDeltaOperation != null ? objectDeltaOperation.getObjectDelta() : null;

        if (objectDelta == null) {
            // TODO what if we just do not have the information?
            return "Focus change execution: none";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Focus change execution: ");
        if (objectDelta.getChangeType() == ChangeTypeType.ADD) {
            sb.append("ADD");
        } else if (objectDelta.getChangeType() == ChangeTypeType.MODIFY) {
            sb.append("MODIFY - ").append(objectDelta.getItemDelta().size()).append(" mod(s)");
        } else if (objectDelta.getChangeType() == ChangeTypeType.DELETE) {
            sb.append("DELETE");
        } else {
            sb.append("?");
        }
        return sb.toString();
    }
}
