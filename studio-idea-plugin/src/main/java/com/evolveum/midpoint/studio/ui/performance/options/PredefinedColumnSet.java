package com.evolveum.midpoint.studio.ui.performance.options;

import com.evolveum.midpoint.studio.ui.performance.mainTree.PerformanceTreeViewColumn;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.evolveum.midpoint.studio.ui.performance.mainTree.PerformanceTreeViewColumn.*;

/**
 *
 */
public enum PredefinedColumnSet {

    ALL("All", Arrays.asList(PerformanceTreeViewColumn.values())),
    COMMON("Common", Arrays.asList(OPERATION_NAME, INVOCATIONS, INVOCATIONS_PER_SAMPLE, TIME_PER_SAMPLE, PERCENT_OF_PARENT)),
    NONE("None", Collections.emptySet());

    private final String name;
    private final Collection<PerformanceTreeViewColumn> columns;

    PredefinedColumnSet(String name, Collection<PerformanceTreeViewColumn> columns) {
        this.name = name;
        this.columns = columns;
    }

    @Override
    public String toString() {
        return name;
    }

    public Collection<PerformanceTreeViewColumn> getColumns() {
        return columns;
    }

    public boolean contains(PerformanceTreeViewColumn column) {
        return columns.contains(column);
    }
}
