package com.evolveum.midpoint.studio.ui.trace;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class ViewingState {

    private Integer selectedIndex;
    private final Collection<TreePath> expandedPaths = new ArrayList<>();

    public Integer getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(Integer selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public Collection<TreePath> getExpandedPaths() {
        return expandedPaths;
    }

    public void addExpandedPath(Object... components) {
        addExpandedPath(new TreePath(components));
    }

    public void addExpandedPath(TreePath expandedPath) {
        expandedPaths.add(expandedPath);
    }

    @Override
    public String toString() {
        return "ViewingState{" +
                "selectedIndex=" + selectedIndex +
                ", expandedPaths=" + expandedPaths +
                '}';
    }
}
