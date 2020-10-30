package com.evolveum.midpoint.studio.impl.performance;

import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

import java.io.Serializable;

/**
 *
 */
public class Invocation implements Serializable {

    private static final long serialVersionUID = -4570085174221980397L;

    final Long elapsedTime;

    final Long cpuTime;

    public Invocation(OperationResultType operationResult) {
        this.elapsedTime = operationResult.getMicroseconds();
        this.cpuTime = null;//operationResult.getCpuMicroseconds();
    }

    public Long getElapsedTime() {
        return elapsedTime;
    }

    public Long getCpuTime() {
        return cpuTime;
    }

    @Override
    public String toString() {
        return "Invocation{" +
                "elapsedTime=" + elapsedTime +
                ", cpuTime=" + cpuTime +
                '}';
    }

    public boolean hasUnknownDuration() {
        return elapsedTime == null || cpuTime == null;
    }
}
