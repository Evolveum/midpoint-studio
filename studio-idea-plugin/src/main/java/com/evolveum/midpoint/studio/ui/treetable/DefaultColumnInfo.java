package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Column definition for use with {@link DefaultTreeTable} / {@link DefaultTreeTableModel}.
 * Supports optional per-cell styling via {@link #style(java.util.function.Function)}.
 *
 * <p>The type parameter {@code UO} is the node user-object type; {@code O} is the column value type.</p>
 *
 * <p>Subclasses that work with JXTreeTable-based trees can override
 * {@link #valueOf(DefaultMutableTreeTableNode)} directly. For trees that use custom nodes
 * (e.g. {@link UserObjectNode}), call {@link #applyValueFunction(Object)} instead.</p>
 */
public class DefaultColumnInfo<UO, O> extends ColumnInfo<DefaultMutableTreeTableNode, O> {

    private final Class<?> type;

    private final Function<UO, O> value;

    private final Function<O, String> valueFormatter;

    private Function<UO, CellStyle> styleProvider;

    private Integer minWidth;
    private Integer maxWidth;
    private Integer preferredWidth;

    public DefaultColumnInfo(@NlsContexts.ColumnName String name) {
        this(name, null);
    }

    public DefaultColumnInfo(@NlsContexts.ColumnName String name, Function<UO, O> value) {
        this(name, String.class, value);
    }

    public DefaultColumnInfo(
            @NlsContexts.ColumnName String name,
            Class<?> type,
            Function<UO, O> value) {
        this(name, type, value, v -> v != null ? v.toString() : null);
    }

    public DefaultColumnInfo(
            @NlsContexts.ColumnName String name,
            Class<?> type,
            Function<UO, O> value,
            Function<O, String> valueFormatter) {
        super(name);
        this.type = type;
        this.value = value;
        this.valueFormatter = valueFormatter;
    }

    @Override
    public Class<?> getColumnClass() {
        return type;
    }

    @Override
    public @Nullable O valueOf(DefaultMutableTreeTableNode node) {
        return value != null ? value.apply((UO) node.getUserObject()) : null;
    }

    /**
     * Applies the value function directly to a user object, bypassing the tree-node wrapper.
     * Used by models whose nodes are not {@link DefaultMutableTreeTableNode}.
     */
    public @Nullable O applyValueFunction(UO userObject) {
        return value != null ? value.apply(userObject) : null;
    }

    // ---- style ----

    /**
     * Returns the cell style for the given node user object, or null for default styling.
     */
    public @Nullable CellStyle getStyle(UO nodeUserObject) {
        return styleProvider != null ? styleProvider.apply(nodeUserObject) : null;
    }

    /**
     * Type-erased variant for use in generic rendering code that only has {@code Object} as user-object type.
     */
    @SuppressWarnings("unchecked")
    public @Nullable CellStyle getStyleUnchecked(Object nodeUserObject) {
        return styleProvider != null ? styleProvider.apply((UO) nodeUserObject) : null;
    }

    public @Nullable java.util.function.Function<UO, CellStyle> getStyleProvider() {
        return styleProvider;
    }

    public DefaultColumnInfo<UO, O> style(java.util.function.Function<UO, CellStyle> styleProvider) {
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
