package com.evolveum.midpoint.studio.ui.trace.options;

import com.evolveum.midpoint.schema.traces.OpType;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.ui.trace.mainTree.TraceTreeViewColumn;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum PredefinedOpView {

    ALL("All", PredefinedOpTypeSet.ALL, PredefinedPerformanceCategoriesSet.NONE, PredefinedColumnSet.ALL),

    FUNCTIONAL_OVERVIEW("Functional overview", PredefinedOpTypeSet.FUNCTIONAL_OVERVIEW,
            PredefinedPerformanceCategoriesSet.NONE, PredefinedColumnSet.FUNCTIONAL_VIEW),

    ASSIGNMENTS_EVALUATION_OVERVIEW("Assignments evaluation overview", PredefinedOpTypeSet.ASSIGNMENTS_EVALUATION_OVERVIEW,
            PredefinedPerformanceCategoriesSet.NONE, PredefinedColumnSet.FUNCTIONAL_VIEW),

    NONE("None", PredefinedOpTypeSet.NONE, PredefinedPerformanceCategoriesSet.NONE, PredefinedColumnSet.FUNCTIONAL_VIEW);

    private final String label;
    private final PredefinedOpTypeSet opTypeSet;
    private final PredefinedPerformanceCategoriesSet categoriesSet;
    private final PredefinedColumnSet columnSet;

    PredefinedOpView(String label, PredefinedOpTypeSet opTypeSet, PredefinedPerformanceCategoriesSet categoriesSet, PredefinedColumnSet columnSet) {
        this.label = label;
        this.opTypeSet = opTypeSet;
        this.categoriesSet = categoriesSet;
        this.columnSet = columnSet;
    }

    public String getLabel() {
        return label;
    }

    public PredefinedOpTypeSet getOpTypeSet() {
        return opTypeSet;
    }

    public PredefinedPerformanceCategoriesSet getCategoriesSet() {
        return categoriesSet;
    }

    public PredefinedColumnSet getColumnSet() {
        return columnSet;
    }

    @NotNull
    public Collection<OpType> getTypes() {
        return opTypeSet.getTypes();
    }

    public Collection<PerformanceCategory> getCategories() {
        return categoriesSet.getCategories();
    }

    public boolean isShowAlsoParents() {
        return categoriesSet.isShowParents();
    }

    public Collection<TraceTreeViewColumn> getColumnsToShow() {
        return columnSet.getColumns();
    }
}
