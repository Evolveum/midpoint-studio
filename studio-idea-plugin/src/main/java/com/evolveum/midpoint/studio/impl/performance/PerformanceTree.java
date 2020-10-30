package com.evolveum.midpoint.studio.impl.performance;

import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

import java.io.Serializable;

/**
 * Operations tree used for performance analysis.
 */
public class PerformanceTree implements Serializable {

    private static final long serialVersionUID = -3391294886060394941L;

    /**
     * Number of samples summarized.
     */
    int currentSampleNumber;

    /**
     * Root of the tree.
     */
    private OperationPerformance root;

    public void addSample(OperationResultType operationResult) {
        OperationKey key = OperationKey.create(operationResult);
        if (root == null) {
            root = new OperationPerformance(key, operationResult, this, null);
        } else if (!root.key.equals(key)) {
            throw new IllegalStateException(
                    "Incompatible operation results: existing root: " + root.key + ", from sample: " + key);
        }

        root.addSample(operationResult);
        currentSampleNumber++;
    }

    public void computeStatistics() {
        if (root != null) {
            root.computeStatistics();
        }
    }

    public int getSamples() {
        return currentSampleNumber;
    }

    public OperationPerformance getRoot() {
        return root;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        if (root != null) {
            root.dump(sb, 0);
        } else {
            sb.append("No root");
        }
        return sb.toString();
    }
}
