package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;

public class MissingRefNode<T> {

    private T value;

    private MissingRefAction action;

    public MissingRefNode(T value, MissingRefAction action) {
        this.value = value;
        this.action = action;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public MissingRefAction getAction() {
        return action;
    }

    public void setAction(MissingRefAction action) {
        this.action = action;
    }
}
