package com.evolveum.midpoint.studio.ui.performance.mainTree.model;

import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * TODO
 */
public class PerformanceTreeTableNode implements TreeTableNode {

    @NotNull private final OperationPerformance opNode;

    private final List<PerformanceTreeTableNode> children = new ArrayList<>();

    /**
     * Parent of this node. Computed anew each time the visibility of nodes change.
     */
    private PerformanceTreeTableNode parent;

    public PerformanceTreeTableNode(@NotNull OperationPerformance opNode) {
        this.opNode = opNode;
    }

    @Override
    public Enumeration<? extends TreeTableNode> children() {
        return Collections.enumeration(children);
    }

    @Override
    public TreeTableNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public TreeTableNode getParent() {
        return parent;
    }

    @Override
    public boolean isEditable(int column) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int column) {
        // no op
    }

    @Override
    public OperationPerformance getUserObject() {
        return opNode;
    }

    @Override
    public void setUserObject(Object userObject) {
        // no op
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        //noinspection SuspiciousMethodCalls
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void clearParentChildLinks() {
        parent = null;
        children.clear();
    }

    public void addChild(PerformanceTreeTableNode child) {
        children.add(child);
        child.setParent(this);
    }

    public void setParent(PerformanceTreeTableNode parent) {
        this.parent = parent;
    }

    public List<PerformanceTreeTableNode> getChildren() {
        return children;
    }

    /*
     * Unimplemented column-related methods.
     *
     * These are not called because TraceTreeTableModel overrides all places where they could be called.
     */
    @Override
    public Object getValueAt(int column) {
        return null;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    /**
     * Temporary implementation.
     */
    @Override
    public String toString() {
        return opNode.toString();
    }
}
