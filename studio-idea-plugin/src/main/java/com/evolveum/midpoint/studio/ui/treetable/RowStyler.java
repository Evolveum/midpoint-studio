package com.evolveum.midpoint.studio.ui.treetable;

import org.jetbrains.annotations.Nullable;

/**
 * Provides row-level cell styling based on the node's data object.
 * Cell-level styles (from {@link DefaultColumnInfo#getStyleProvider()}) take priority over row styles.
 *
 * @param <N> type of the node's user object
 */
@FunctionalInterface
public interface RowStyler<N> {

    /**
     * Returns styling for the row corresponding to the given node user object.
     * Return null for no special styling.
     */
    @Nullable
    CellStyle getStyle(N node);
}
