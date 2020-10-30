package com.evolveum.midpoint.studio.impl.performance;

import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Performance of an operation executions in a given sample (trace).
 */
public class SampleInformation implements Serializable {

    private static final long serialVersionUID = -5680246966886460134L;

    /**
     * Beware, there can be some nulls here (if the value was not known).
     */
    @NotNull final List<Invocation> invocations = new ArrayList<>();

    public void addOperationResult(OperationResultType operationResult) {
        invocations.add(new Invocation(operationResult));
    }

    public int getInvocationCount() {
        return invocations.size();
    }

    public long getTotalElapsedTime() {
        long total = 0;
        for (Invocation invocation : invocations) {
            if (invocation != null && invocation.getElapsedTime() != null) {
                total += invocation.getElapsedTime();
            }
        }
        return total;
    }

    public long getTotalCpuTime() {
        long total = 0;
        for (Invocation invocation : invocations) {
            if (invocation != null && invocation.getCpuTime() != null) {
                total += invocation.getCpuTime();
            }
        }
        return total;
    }

    public boolean hasUnknownDurations() {
        return invocations.stream().anyMatch(Invocation::hasUnknownDuration);
    }
}
