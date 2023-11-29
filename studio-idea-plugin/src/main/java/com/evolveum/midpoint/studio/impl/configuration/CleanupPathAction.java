package com.evolveum.midpoint.studio.impl.configuration;

import java.util.Arrays;

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

    public static CleanupPathAction getState(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(s -> value.equals(s.value()))
                .findFirst()
                .orElse(null);
    }
}
