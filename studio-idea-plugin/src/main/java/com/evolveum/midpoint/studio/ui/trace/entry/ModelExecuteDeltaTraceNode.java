package com.evolveum.midpoint.studio.ui.trace.entry;

import com.evolveum.midpoint.xml.ns._public.common.common_3.LensObjectDeltaOperationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ModelExecuteDeltaTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectDeltaOperationType;

public class ModelExecuteDeltaTraceNode extends TraceNode<ModelExecuteDeltaTraceType> {

	public ModelExecuteDeltaTraceNode(ModelExecuteDeltaTraceType trace) {
		super(trace);

		createChildren();
	}
	
	public void createChildren() {
		LensObjectDeltaOperationType lensOdo = trace.getDelta();
		if (lensOdo == null) {
			return;
		}
		ObjectDeltaOperationType odo = lensOdo.getObjectDeltaOperation();
		if (odo != null) {
			TextNode.create("Object name", odo.getObjectName(), this);
			if (odo.getResourceName() != null || odo.getResourceOid() != null) {
				TextNode.create("Resource", odo.getResourceName() + " (" + odo.getResourceOid() + ")", this);
			}
			if (odo.getObjectDelta() != null) {
				TextNode.create("Delta", odo.getObjectDelta(), this);
			}
			if (odo.getExecutionResult() != null) {
				TextNode.create("Result", odo.getExecutionResult().getStatus(), this);
			}
		}
		TextNode.create("Audited", lensOdo.isAudited(), this);
	}

}
