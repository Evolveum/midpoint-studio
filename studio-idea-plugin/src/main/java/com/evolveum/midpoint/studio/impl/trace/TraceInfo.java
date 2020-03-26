package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceDictionaryEntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TracingOutputType;

public class TraceInfo {
	private final TracingOutputType tracingOutput;
	
	public TraceInfo(TracingOutputType tracingOutput) {
		this.tracingOutput = tracingOutput;
	}

	public TracingOutputType getTracingOutput() {
		return tracingOutput;
	}

	public PrismObject<?> findObject(String oid) {
		if (oid == null || tracingOutput == null || tracingOutput.getDictionary() == null) {
			return null;
		}
		for (TraceDictionaryEntryType entry : tracingOutput.getDictionary().getEntry()) {
			if (oid.equals(entry.getObject().getOid())) {
				PrismObject<?> object = entry.getObject().getObject();
				if (object != null) {
					return object;
				}
			}
		}
		return null;
	}
}
