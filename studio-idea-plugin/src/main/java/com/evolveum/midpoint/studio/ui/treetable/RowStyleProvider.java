package com.evolveum.midpoint.studio.ui.treetable;

import org.jetbrains.annotations.Nullable;

/**
 * Provides row-level cell styling based on the raw tree node object.
 * The node is the actual tree node (e.g. a {@code Node<?>} or {@code AbstractOpTreeTableNode});
 * callers should use {@code instanceof} to extract what they need.
 * Cell-level styles (from {@link DefaultColumnInfo#getStyleProvider()}) take priority over row styles.
 */
@FunctionalInterface
public interface RowStyleProvider {

    /**
     * Returns styling for the row corresponding to the given tree node.
     * Return null for no special styling.
     */
    @Nullable
    Style getStyle(Object node);
}
