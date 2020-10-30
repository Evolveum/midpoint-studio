package com.evolveum.midpoint.studio.impl.performance;

import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.schema.traces.PerformanceCategoryInfo;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

/**
 * Information about (summarized) operation performance.
 *
 * This class is analogous to OpNode. The main difference is that this class represents AGGREGATION (summarization) of compatible operations.
 */
public class OperationPerformance implements Serializable {

    private static final long serialVersionUID = 7269151436540308917L;

    /**
     * Characterization of the operation used as a summarization key. Contains name, qualifiers, and selected parameters and
     * context values.
     */
    @NotNull final OperationKey key;

    /**
     * Performance tree to which this operation belong.
     */
    @NotNull final PerformanceTree tree;

    /**
     * Parent operation.
     */
    @Nullable final OperationPerformance parent;

    /**
     * Child operations - i.e. operations that were called from the current one.
     */
    @NotNull final Map<OperationKey, OperationPerformance> children = new LinkedHashMap<>();

    /**
     * Categorization of this operation result regarding performance.
     */
    @NotNull final Set<PerformanceCategory> categories = new HashSet<>();

    /**
     * Information about execution(s) of this operation in individual samples.
     * Each sample results in a single OperationInSample record, even if the operation is executed in that sample more times.
     */
    @NotNull final List<SampleInformation> sampleInformationList = new ArrayList<>();

    /**
     * Computed statistics related to this operation.
     */
    private OperationStatistics operationStatistics;

    public OperationPerformance(@NotNull OperationKey key, @NotNull OperationResultType operationResult,
            @NotNull PerformanceTree tree, @Nullable OperationPerformance parent) {
        this.key = key;
        this.tree = tree;
        this.parent = parent;
        categorize(operationResult);
    }

    /**
     * We should categorize simply on OperationKey, not the whole OperationResultType. But, unfortunately, the
     * {@link PerformanceCategory#matches(OperationResultType)} method expects the whole result. This will be
     * probably changed in the future.
     */
    private void categorize(@NotNull OperationResultType operationResult) {
        for (PerformanceCategory category : PerformanceCategory.values()) {
            if (category.matches(operationResult)) {
                categories.add(category);
            }
        }
    }

    public void addSample(OperationResultType operationResult) {
        assert key.operationName.equals(operationResult.getOperation()); // other parts of the key should match as well (this is just a quick test)

        addSampleInformation(operationResult);
        for (OperationResultType subresult : operationResult.getPartialResults()) {
            addSampleSubresult(subresult);
        }
    }

    private void addSampleInformation(OperationResultType operationResult) {
        while (sampleInformationList.size() <= tree.currentSampleNumber) {
            sampleInformationList.add(null);
        }
        SampleInformation current = sampleInformationList.get(tree.currentSampleNumber);
        if (current == null) {
            current = new SampleInformation();
            sampleInformationList.set(tree.currentSampleNumber, current);
        }
        current.addOperationResult(operationResult);
    }

    private void addSampleSubresult(OperationResultType subresult) {
        OperationKey subKey = OperationKey.create(subresult);
        OperationPerformance subOp = children.computeIfAbsent(subKey, key -> new OperationPerformance(key, subresult, tree, this));
        subOp.addSample(subresult);
    }

    /**
     * PRECONDITION: children are already computed
     */
    public long getElapsedTimeInChildren() {
        long time = 0;
        for (OperationPerformance child : children.values()) {
            time += child.operationStatistics.totalTime;
        }
        return time;
    }

    /**
     * PRECONDITION: children are already computed
     */
    public long getCpuTimeInChildren() {
        long time = 0;
        for (OperationPerformance child : children.values()) {
            time += child.operationStatistics.totalCpuTime;
        }
        return time;
    }

    /**
     * Computes operation statistics (recursively).
     */
    void computeStatistics() {
        assert operationStatistics == null;
        children.values().forEach(OperationPerformance::computeStatistics);
        operationStatistics = OperationStatistics.compute(this);
    }

    @Override
    public String toString() {
        return "OperationPerformance{" +
                "key=" + key +
                ", children=" + children.size() +
                ", operationStatistics=" + operationStatistics +
                '}';
    }

    public void dump(StringBuilder sb, int indent) {
        sb.append(StringUtils.repeat(' ', indent));
        sb.append(toString());
        sb.append("\n");
        children.values().forEach(child -> child.dump(sb,indent + 1));
    }

    public @NotNull OperationKey getKey() {
        return key;
    }

    public Collection<OperationPerformance> getChildren() {
        return children.values();
    }

    public OperationStatistics getOperationStatistics() {
        return operationStatistics;
    }

    public @NotNull PerformanceTree getTree() {
        return tree;
    }

    public @Nullable OperationPerformance getParent() {
        return parent;
    }

    public @NotNull List<SampleInformation> getSampleInformationList() {
        return sampleInformationList;
    }

    public Map<PerformanceCategory, PerformanceCategoryInfo> getPerformanceByCategory() {
        return operationStatistics.getPerformanceByCategory();
    }
}
