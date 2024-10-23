package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.Function;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;

public class DefaultColumnInfo<UO, O> extends ColumnInfo<DefaultMutableTreeTableNode, O> {

    private Function<UO, O> valueOf;

    private Integer minWidth;
    private Integer maxWidth;
    private Integer preferredWidth;

    public DefaultColumnInfo(@NlsContexts.ColumnName String name) {
        this(name, null);
    }

    public DefaultColumnInfo(@NlsContexts.ColumnName String name, Function<UO, O> valueOf) {
        super(name);

        this.valueOf = valueOf;
    }

    @Override
    public @Nullable O valueOf(DefaultMutableTreeTableNode node) {
        return valueOf != null ? valueOf.fun((UO) node.getUserObject()) : null;
    }

    public Integer getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
    }

    public DefaultColumnInfo minWidth(Integer minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    public DefaultColumnInfo maxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Integer getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(Integer preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public DefaultColumnInfo preferredWidth(Integer preferredWidth) {
        this.preferredWidth = preferredWidth;
        return this;
    }
}
