package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.OpType;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.schema.traces.PerformanceCategoryInfo;
import com.evolveum.midpoint.studio.ui.trace.mainTree.TraceTreeViewColumn;

import java.util.HashSet;
import java.util.Set;

public class Options implements Cloneable {

    private final Set<OpType> typesToShow = new HashSet<>();

    private final Set<PerformanceCategory> categoriesToShow = new HashSet<>();

    private boolean showAlsoParents;

    private final Set<TraceTreeViewColumn> columnsToShow = new HashSet<>();

    public boolean isShowAlsoParents() {
        return showAlsoParents;
    }

    public void setShowAlsoParents(boolean showAlsoParents) {
        this.showAlsoParents = showAlsoParents;
    }

    public Set<OpType> getTypesToShow() {
        return typesToShow;
    }

    public Set<PerformanceCategory> getCategoriesToShow() {
        return categoriesToShow;
    }

    public Set<TraceTreeViewColumn> getColumnsToShow() {
        return columnsToShow;
    }

    public Options() {
    }

    public Options(Options original) {
        this.typesToShow.addAll(original.typesToShow);
        this.categoriesToShow.addAll(original.categoriesToShow);
        this.showAlsoParents = original.showAlsoParents;
        this.columnsToShow.addAll(original.columnsToShow);
    }

    public boolean nodeVisibilityDiffers(Options other) {
        return !typesToShow.equals(other.typesToShow) ||
                !categoriesToShow.equals(other.categoriesToShow) ||
                showAlsoParents != other.showAlsoParents;
    }

    public void applyVisibilityTo(OpNode root) {
        root.setVisible(isVisible(root));
        for (OpNode child : root.getChildren()) {
            applyVisibilityTo(child);
        }
    }

    private boolean isVisible(OpNode node) {
        if (getTypesToShow().contains(node.getType())) {
            return true;
        }
        for (PerformanceCategory cat : getCategoriesToShow()) {
            PerformanceCategoryInfo perfInfo = node.getPerformanceByCategory().get(cat);
            if (isShowAlsoParents()) {
                if (perfInfo.getTotalCount() > 0) {
                    return true;
                }
            } else {
                if (perfInfo.getOwnCount() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings({ "MethodDoesntCallSuperMethod" })
    @Override
    public Options clone() {
        return new Options(this);
    }
}
