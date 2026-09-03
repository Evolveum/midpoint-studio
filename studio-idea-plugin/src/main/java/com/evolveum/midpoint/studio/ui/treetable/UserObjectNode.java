package com.evolveum.midpoint.studio.ui.treetable;

/**
 * Marker interface for tree nodes that carry a user object.
 * Allows {@link DefaultTreeTable#prepareRenderer} to apply styling without
 * requiring nodes to extend {@link javax.swing.tree.DefaultMutableTreeNode}.
 */
public interface UserObjectNode {

    Object getUserObject();
}
