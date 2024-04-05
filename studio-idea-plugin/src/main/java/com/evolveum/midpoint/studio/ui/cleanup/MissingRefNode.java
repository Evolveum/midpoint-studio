package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;

import java.util.Objects;

public class MissingRefNode<T> {

    private T value;

    private MissingRefAction action;

    public MissingRefNode(T value) {
        this(value, MissingRefAction.UNDEFINED);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissingRefNode<?> that = (MissingRefNode<?>) o;
        return Objects.equals(value, that.value) && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, action);
    }

    @Override
    public String toString() {
        return "MissingRefNode{" +
                "action=" + action +
                ", value=" + value +
                '}';
    }
}
