package com.evolveum.midpoint.studio.impl.performance;

import com.evolveum.midpoint.studio.ui.performance.mainTree.PerformanceTreeViewColumn;

import java.util.HashSet;
import java.util.Set;

public class PerformanceOptions implements Cloneable {

    private final Set<PerformanceTreeViewColumn> columnsToShow = new HashSet<>();

    public Set<PerformanceTreeViewColumn> getColumnsToShow() {
        return columnsToShow;
    }

    public PerformanceOptions() {
    }

    public PerformanceOptions(PerformanceOptions original) {
        this.columnsToShow.addAll(original.columnsToShow);
    }

    @SuppressWarnings({ "MethodDoesntCallSuperMethod" })
    @Override
    public PerformanceOptions clone() {
        return new PerformanceOptions(this);
    }
}
