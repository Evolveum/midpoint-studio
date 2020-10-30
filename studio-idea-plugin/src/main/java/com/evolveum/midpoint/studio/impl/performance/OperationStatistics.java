package com.evolveum.midpoint.studio.impl.performance;

import com.evolveum.midpoint.schema.statistics.OperationsPerformanceInformationUtil;
import com.evolveum.midpoint.schema.traces.OpResultInfo;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.schema.traces.PerformanceCategoryInfo;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationsPerformanceInformationType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Computed statistics related to a given operation in operation tree.
 */
public class OperationStatistics implements Serializable {

    private static final long serialVersionUID = 3324285081838528146L;

    /**
     * Owner node.
     */
    @NotNull final OperationPerformance owner;

    /**
     * Number of samples considered.
     */
    final int samples;

    /**
     * Total number of invocations.
     */
    final int invocations;

    /**
     * How many samples contain at least one invocation of the operation.
     */
    final int presence;

    /**
     * Total time spent in this operation.
     */
    final long totalTime;

    /**
     * Total CPU time spent in this operation.
     */
    final long totalCpuTime;

    /**
     * Total time spent in this operation, NOT counting time spent in the children.
     */
    final long ownTime;

    /**
     * Total CPU time spent in this operation, NOT counting time spent in the children.
     */
    final long ownCpuTime;

    /**
     * Minimum time spent here in any sample with at least one invocation (may cover multiple invocations).
     */
    final Long minTime;

    /**
     * Maximum time spent here in any sample (may cover multiple invocations).
     */
    final Long maxTime;

    /**
     * Whether this operation contains some executions with unknown execution time.
     * (Indicates a problem in data collection.)
     */
    final boolean hasUnknownDurations;

    /**
     * Performance information for individual categories, collected from the whole subtree.
     */
    final Map<PerformanceCategory, PerformanceCategoryInfo> performanceByCategory = new HashMap<>();

    /**
     * Performance information for individual operations, collected from the whole subtree.
     */
    final OperationsPerformanceInformationType performanceByOperation = new OperationsPerformanceInformationType();

    private OperationStatistics(@NotNull OperationPerformance owner, int samples, int invocations, int presence, long totalTime,
            long totalCpuTime, long ownTime, long ownCpuTime, Long minTime, Long maxTime, boolean hasUnknownDurations) {
        this.owner = owner;
        this.samples = samples;
        this.invocations = invocations;
        this.presence = presence;
        this.totalTime = totalTime;
        this.totalCpuTime = totalCpuTime;
        this.ownTime = ownTime;
        this.ownCpuTime = ownCpuTime;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.hasUnknownDurations = hasUnknownDurations;
    }

    /**
     * Assumes that children have their statistics already computed.
     */
    static OperationStatistics compute(OperationPerformance owner) {
        int samples = owner.tree.currentSampleNumber;
        int invocations = 0, presence = 0;
        long totalElapsedTime = 0, totalCpuTime = 0;
        Long minElapsedTime = null, maxElapsedTime = null;
        boolean hasUnknownDurations = false;
        for (SampleInformation sampleInformation : owner.sampleInformationList) {
            if (sampleInformation != null) {
                hasUnknownDurations = hasUnknownDurations || sampleInformation.hasUnknownDurations();
                totalElapsedTime += sampleInformation.getTotalElapsedTime();
                totalCpuTime += sampleInformation.getTotalCpuTime();
                if (minElapsedTime == null || totalElapsedTime < minElapsedTime) {
                    minElapsedTime = totalElapsedTime;
                }
                if (maxElapsedTime == null || totalElapsedTime > maxElapsedTime) {
                    maxElapsedTime = totalElapsedTime;
                }
                int invocationCount = sampleInformation.getInvocationCount();
                if (invocationCount > 0) {
                    invocations += invocationCount;
                    presence++;
                }
            }
        }
        long ownElapsedTime = totalElapsedTime - owner.getElapsedTimeInChildren();
        long ownCpuTime = totalCpuTime - owner.getCpuTimeInChildren();
        OperationStatistics operationStatistics = new OperationStatistics(owner, samples, invocations, presence, totalElapsedTime,
                totalCpuTime, ownElapsedTime, ownCpuTime,
                minElapsedTime, maxElapsedTime, hasUnknownDurations);

        operationStatistics.computePerformanceByCategory();
        operationStatistics.computePerformanceByOperation();
        return operationStatistics;
    }

    /**
     * See analogous code in {@link OpResultInfo}.
     */
    private void computePerformanceByCategory() {
        for (PerformanceCategory category : PerformanceCategory.values()) {
            performanceByCategory.put(category, computePerformanceFor(category));
        }
    }

    private PerformanceCategoryInfo computePerformanceFor(PerformanceCategory category) {
        PerformanceCategoryInfo rv = new PerformanceCategoryInfo();
        if (owner.categories.contains(category)) {
            rv.setOwnCount(invocations);
            rv.setOwnTime(totalTime);
            rv.setTotalCount(invocations);
            rv.setTotalTime(totalTime);
        } else {
            int totalInvocationsForCategory = 0;
            long totalTimeForCategory = 0;
            for (OperationPerformance child : owner.getChildren()) {
                PerformanceCategoryInfo childPerformance = child.getOperationStatistics().getPerformanceFor(category);
                totalInvocationsForCategory += childPerformance.getTotalCount();
                totalTimeForCategory += childPerformance.getTotalTime();
            }
            rv.setTotalCount(totalInvocationsForCategory);
            rv.setTotalTime(totalTimeForCategory);
        }
        return rv;
    }

    /**
     * See analogous code in {@link OpResultInfo}.
     */
    private void computePerformanceByOperation() {
        performanceByOperation.beginOperation()
                .name(owner.getKey().getFormattedName())
                .invocationCount(invocations)
                .totalTime(totalTime)
                .minTime(minTime)
                .maxTime(maxTime);
        for (OperationPerformance child : owner.getChildren()) {
            OperationsPerformanceInformationUtil.addTo(performanceByOperation, child.getOperationStatistics().performanceByOperation);
        }
    }

    public int getSamples() {
        return samples;
    }

    public int getInvocations() {
        return invocations;
    }

    public int getPresence() {
        return presence;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getTotalCpuTime() {
        return totalCpuTime;
    }

    public long getOwnTime() {
        return ownTime;
    }

    public long getOwnCpuTime() {
        return ownCpuTime;
    }

    public Long getMinTime() {
        return minTime;
    }

    public Long getMaxTime() {
        return maxTime;
    }

    public double getAvgTimeOverall() {
        return (double) totalTime / samples;
    }

    public double getAvgOwnTimeOverall() {
        return (double) ownTime / samples;
    }

    public Double getAvgTimeWhenPresent() {
        if (presence > 0) {
            return (double) totalTime / presence;
        } else {
            return null;
        }
    }

    public Double getAvgOwnTimeWhenPresent() {
        if (presence > 0) {
            return (double) ownTime / presence;
        } else {
            return null;
        }
    }

    public boolean hasUnknownDurations() {
        return hasUnknownDurations;
    }

    @Override
    public String toString() {
        return "OperationStatistics{" +
                "invocations=" + invocations +
                ", presence=" + presence +
                ", totalTime=" + totalTime +
                ", ownTime=" + ownTime +
                ", minTime=" + minTime +
                ", maxTime=" + maxTime +
                ", hasUnknownDurations=" + hasUnknownDurations +
                '}';
    }

    public double getRatioOfParent() {
        if (owner.parent != null) {
            long parentTime = owner.parent.getOperationStatistics().totalTime;
            return (double) this.totalTime / parentTime;
        } else {
            return 1;
        }
    }

    public double getRatioOfRoot() {
        long rootTime = owner.tree.getRoot().getOperationStatistics().totalTime;
        return (double) this.totalTime / rootTime;
    }

    public Map<PerformanceCategory, PerformanceCategoryInfo> getPerformanceByCategory() {
        return performanceByCategory;
    }

    public PerformanceCategoryInfo getPerformanceFor(PerformanceCategory category) {
        return performanceByCategory.get(category);
    }

    public OperationsPerformanceInformationType getPerformanceByOperation() {
        return performanceByOperation;
    }
}
