package com.evolveum.midpoint.sdk.api.lang;

import java.util.Objects;

public class Variable<T, I extends T> {

    private String name;

    private Class<T> type;

    private Class<I> implementation;

    public Variable(String name, Class<T> type, Class<I> implementation) {
        this.name = name;
        this.type = type;
        this.implementation = implementation;
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    public Class<I> implementation() {
        return implementation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable<?, ?> variable = (Variable<?, ?>) o;
        return Objects.equals(name, variable.name) && Objects.equals(type, variable.type) && Objects.equals(implementation, variable.implementation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, implementation);
    }
}