package com.evolveum.midpoint.studio.ui.trace.options;

import com.evolveum.midpoint.studio.ui.trace.lens.TraceTreeViewColumn;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.evolveum.midpoint.studio.ui.trace.lens.TraceTreeViewColumn.*;

/**
 *
 */
public enum PredefinedColumnSet {

    ALL("All", Arrays.asList(TraceTreeViewColumn.values())),
    FUNCTIONAL_VIEW("Functional view", Arrays.asList(OPERATION_NAME, CLOCKWORK_STATE, EXECUTION_WAVE, STATUS, MAP_COUNT, REPO_W_COUNT, ICF_R_COUNT, ICF_W_COUNT)),
    NONE("None", Collections.emptySet());

    private final String name;
    private final Collection<TraceTreeViewColumn> columns;

    PredefinedColumnSet(String name, Collection<TraceTreeViewColumn> columns) {
        this.name = name;
        this.columns = columns;
    }

    @Override
    public String toString() {
        return name;
    }

    public Collection<TraceTreeViewColumn> getColumns() {
        return columns;
    }

    public boolean contains(TraceTreeViewColumn column) {
        return columns.contains(column);
    }
}
