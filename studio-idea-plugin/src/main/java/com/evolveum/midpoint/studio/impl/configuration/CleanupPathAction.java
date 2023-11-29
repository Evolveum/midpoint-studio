package com.evolveum.midpoint.studio.impl.configuration;

public enum CleanupPathAction {

    REMOVE("remove"),

    IGNORE("ignore"),

    ASK("ask");

    private final String value;

    CleanupPathAction(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
