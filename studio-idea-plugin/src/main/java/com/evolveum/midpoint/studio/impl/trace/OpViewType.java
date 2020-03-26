package com.evolveum.midpoint.studio.impl.trace;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Collections.emptySet;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum OpViewType {

    ALL("All", null, null, true, true, true),

    OVERVIEW("Overview", Arrays.asList(OpType.CLOCKWORK_RUN, OpType.MAPPING_EVALUATION, OpType.CHANGE_EXECUTION_SUB, OpType.FOCUS_LOAD, OpType.SHADOW_LOAD), emptySet(), false, false, false),

    NONE("None", emptySet(), emptySet(), false, false, false);

    private final String label;
    private final Collection<OpType> types;
    private final Collection<PerformanceCategory> categories;
    private final boolean showAlsoParents;
    private final boolean showPerformanceColumns;
    private final boolean showReadWriteColumns;

    private OpViewType(String label, Collection<OpType> types, Collection<PerformanceCategory> categories, boolean showAlsoParents, boolean showPerformanceColumns,
                       boolean showReadWriteColumns) {
        this.label = label;
        this.types = types;
        this.categories = categories;
        this.showAlsoParents = showAlsoParents;
        this.showPerformanceColumns = showPerformanceColumns;
        this.showReadWriteColumns = showReadWriteColumns;
    }

    public String getLabel() {
        return label;
    }

    public Collection<OpType> getTypes() {
        return types;
    }

    public Collection<PerformanceCategory> getCategories() {
        return categories;
    }

    public boolean isShowAlsoParents() {
        return showAlsoParents;
    }

    public boolean isShowPerformanceColumns() {
        return showPerformanceColumns;
    }

    public boolean isShowReadWriteColumns() {
        return showReadWriteColumns;
    }
}
