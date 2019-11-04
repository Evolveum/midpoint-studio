package com.evolveum.midpoint.studio.ui.trace;

import java.util.function.Function;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TreeTableColumnDefinition<R, V> {

    private String header;

    private int size;

    private Function<R, V> value;

    public TreeTableColumnDefinition(String header, int size, Function<R, V> value) {
        this.header = header;
        this.size = size;
        this.value = value;
    }

    public String getHeader() {
        return header;
    }

    public int getSize() {
        return size;
    }

    public Function<R, V> getValue() {
        return value;
    }
}
