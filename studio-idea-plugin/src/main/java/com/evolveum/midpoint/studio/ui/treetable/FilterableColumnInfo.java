package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.Function;

public class FilterableColumnInfo<UO, O> extends DefaultColumnInfo<UO, O> {

    public FilterableColumnInfo(@NlsContexts.ColumnName String name, Function<UO, O> valueOf) {
        super(name, valueOf);
    }
}
