package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Common ancestor for trace tree table nodes.
 */
public class AbstractTraceTreeTableNode implements TreeTableNode {

    /**
     * OpNode corresponding to this trace tree table node.
     * Always non-null for regular (non-root i.e. potentially visible) nodes.
     */
    @Nullable private final OpNode opNode;

    /**
     * VISIBLE children for this node. (Note that this node itself may or may not be visible.)
     * Computed anew each time the visibility of nodes change.
     */
    private final List<RegularTraceTreeTableNode> children = new ArrayList<>();

    /**
     * Parent of this node. Computed anew each time the visibility of nodes change.
     */
    private AbstractTraceTreeTableNode parent;

    public AbstractTraceTreeTableNode(@Nullable OpNode opNode) {
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
    public OpNode getUserObject() {
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

    public void addChild(RegularTraceTreeTableNode child) {
//        System.out.println("Adding child " + child + " to " + this);
        children.add(child);
        child.setParent(this);
    }

    public void setParent(AbstractTraceTreeTableNode parent) {
        this.parent = parent;
    }

    public List<RegularTraceTreeTableNode> getChildren() {
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
        return opNode != null ? opNode.getOperationNameFormatted() + " (" + opNode.getInvocationId() + ")" : "(null)";
    }
}
