package com.evolveum.midpoint.studio.ui.browse;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.Function;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;

public class ObjectColumnInfo<UserObject, Object> extends ColumnInfo<DefaultMutableTreeTableNode, Object> {

    private Function<UserObject, Object> valueOf;

    public ObjectColumnInfo(@NlsContexts.ColumnName String name) {
        this(name, null);
    }

    public ObjectColumnInfo(@NlsContexts.ColumnName String name, Function<UserObject, Object> valueOf) {
        super(name);

        this.valueOf = valueOf;
    }

    @Override
    public @Nullable Object valueOf(DefaultMutableTreeTableNode node) {
        return valueOf != null ? valueOf.fun((UserObject) node.getUserObject()) : null;
    }
}
