package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpType;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public enum PredefinedPerformanceCategoriesSet {

    ALL("All", true, Arrays.asList(PerformanceCategory.values())),
    NONE("None", false, Collections.emptySet());

    private final String name;
    private final boolean showParents;
    private final Collection<PerformanceCategory> categories;

    PredefinedPerformanceCategoriesSet(String name, boolean showParents, Collection<PerformanceCategory> categories) {
        this.name = name;
        this.showParents = showParents;
        this.categories = categories;
    }

    @Override
    public String toString() {
        return name;
    }

    public Collection<PerformanceCategory> getCategories() {
        return categories;
    }

    public boolean isShowParents() {
        return showParents;
    }

    public boolean contains(PerformanceCategory category) {
        return categories.contains(category);
    }
}
