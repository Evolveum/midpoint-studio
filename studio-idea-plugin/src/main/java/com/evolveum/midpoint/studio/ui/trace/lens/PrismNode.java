package com.evolveum.midpoint.studio.ui.trace.lens;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Base tree node for the lens context tree. Carries label and per-column
 * string values (old / current / new).
 */
public abstract class PrismNode extends DefaultMutableTreeNode {

    public PrismNode(PrismNode parent) {
        if (parent != null) {
            parent.add(this);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PrismNode> getChildren() {
        Enumeration<javax.swing.tree.TreeNode> e = children();
        if (e == null) {
            return new ArrayList<>();
        }
        return (List<PrismNode>) (List<?>) Collections.list(e);
    }

    public abstract String getLabel();

    /** Returns the value for value-column {@code i} (0 = old, 1 = current, 2 = new). */
    public abstract String getValue(int i);
}
