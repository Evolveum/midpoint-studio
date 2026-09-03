package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.function.Function;

/**
 * Column definition for use with {@link DefaultTreeTable} / {@link DefaultTreeTableModel}.
 * Supports optional per-cell styling via {@link #style(java.util.function.Function)}.
 *
 * <p>The type parameter {@code UO} is the node user-object type; {@code O} is the column value type.</p>
 *
 * <p>Subclasses can override {@link #valueOf(MutableTreeNode)} directly, or for trees that use
 * custom nodes (e.g. {@link UserObjectNode}), call {@link #applyValueFunction(Object)} instead.</p>
 */
public class DefaultColumnInfo<UO, O> extends ColumnInfo<MutableTreeNode, O> {

    private final Class<?> type;

    private final Function<UO, O> valueProvider;

    private Function<UO, Style> styleProvider;

    private Integer minWidth;
    private Integer maxWidth;
    private Integer preferredWidth;

    public DefaultColumnInfo(@NlsContexts.ColumnName String name) {
        this(name, null);
    }

    public DefaultColumnInfo(@NlsContexts.ColumnName String name, Function<UO, O> valueProvider) {
        this(name, String.class, valueProvider);
    }

    public DefaultColumnInfo(
            @NlsContexts.ColumnName String name,
            Class<?> type,
            Function<UO, O> valueProvider) {
        super(name);
        this.type = type;
        this.valueProvider = valueProvider;
    }

    @Override
    public Class<?> getColumnClass() {
        return type;
    }

    protected UO getUserObject(@NotNull MutableTreeNode node) {
        if (node instanceof UserObjectNode uon) {
            return (UO) uon.getUserObject();
        }

        if (node instanceof DefaultMutableTreeNode dmtn) {
            return (UO) dmtn.getUserObject();
        }

        throw new IllegalArgumentException("Node doesn't have user object");
    }

    @Override
    public @Nullable O valueOf(MutableTreeNode node) {
        UO object = getUserObject(node);
        return valueProvider != null ? valueProvider.apply(object) : null;
    }

    /**
     * Applies the value function directly to a user object, bypassing the tree-node wrapper.
     * Used by models whose nodes are not {@link javax.swing.tree.DefaultMutableTreeNode}.
     */
    public @Nullable O applyValueFunction(UO userObject) {
        return valueProvider != null ? valueProvider.apply(userObject) : null;
    }

    // ---- style ----

    /**
     * Returns the cell style for the given node user object, or null for default styling.
     */
    public @Nullable Style getStyle(UO nodeUserObject) {
        return styleProvider != null ? styleProvider.apply(nodeUserObject) : null;
    }

    /**
     * Type-erased variant for use in generic rendering code that only has {@code Object} as user-object type.
     */
    @SuppressWarnings("unchecked")
    public @Nullable Style getStyleUnchecked(Object nodeUserObject) {
        return styleProvider != null ? styleProvider.apply((UO) nodeUserObject) : null;
    }

    public @Nullable java.util.function.Function<UO, Style> getStyleProvider() {
        return styleProvider;
    }

    public DefaultColumnInfo<UO, O> style(java.util.function.Function<UO, Style> styleProvider) {
        this.styleProvider = styleProvider;
        return this;
    }

    // ---- width ----

    public Integer getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
    }

    public DefaultColumnInfo<UO, O> minWidth(Integer minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    public DefaultColumnInfo<UO, O> maxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Integer getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(Integer preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public DefaultColumnInfo<UO, O> preferredWidth(Integer preferredWidth) {
        this.preferredWidth = preferredWidth;
        return this;
    }
}
