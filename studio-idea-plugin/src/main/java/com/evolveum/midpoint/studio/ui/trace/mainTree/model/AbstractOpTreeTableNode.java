package com.evolveum.midpoint.studio.ui.trace.mainTree.model;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.ui.treetable.UserObjectNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Common ancestor for trace tree table nodes.
 * Implements standard Swing MutableTreeNode and {@link UserObjectNode}.
 */
public class AbstractOpTreeTableNode implements MutableTreeNode, UserObjectNode {

    /**
     * OpNode corresponding to this trace tree table node.
     * Always non-null for regular (non-root i.e. potentially visible) nodes.
     */
    @Nullable private final OpNode opNode;

    /**
     * VISIBLE children for this node. (Note that this node itself may or may not be visible.)
     * Computed anew each time the visibility of nodes change.
     */
    private final List<RegularOpTreeTableNode> children = new ArrayList<>();

    /** Parent of this node. Computed anew each time visibility changes. */
    private AbstractOpTreeTableNode parent;

    public AbstractOpTreeTableNode(@Nullable OpNode opNode) {
        this.opNode = opNode;
    }

    // ---- TreeNode ----

    @Override
    public Enumeration<? extends TreeNode> children() {
        return Collections.enumeration(children);
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public TreeNode getParent() {
        return parent;
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
        return true;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    // ---- MutableTreeNode ----

    @Override
    public void insert(MutableTreeNode child, int index) {
        // managed via addChild
    }

    @Override
    public void remove(int index) {
        children.remove(index);
    }

    @Override
    public void remove(MutableTreeNode node) {
        //noinspection SuspiciousMethodCalls
        children.remove(node);
    }

    @Override
    public void setUserObject(Object userObject) {
        // immutable — set via constructor
    }

    @Override
    public void removeFromParent() {
        if (parent != null) {
            parent.remove(this);
            parent = null;
        }
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        this.parent = (AbstractOpTreeTableNode) newParent;
    }

    // ---- UserObjectNode / getUserObject ----

    @Override
    @Nullable
    public OpNode getUserObject() {
        return opNode;
    }

    // ---- internal link management ----

    public void clearParentChildLinks() {
        parent = null;
        children.clear();
    }

    public void addChild(RegularOpTreeTableNode child) {
        children.add(child);
        child.setParent(this);
    }

    public void setParent(AbstractOpTreeTableNode parent) {
        this.parent = parent;
    }

    public List<RegularOpTreeTableNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return opNode != null
                ? opNode.getOperationNameFormatted() + " (" + opNode.getInvocationId() + ")"
                : "(null)";
    }
}
