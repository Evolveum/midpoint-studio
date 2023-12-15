package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.common.cleanup.CleanupPathAction;

public enum CleanupPathActionConfiguration {

    REMOVE(CleanupPathAction.REMOVE),

    IGNORE(CleanupPathAction.IGNORE),

    ASK(CleanupPathAction.ASK);

    private final CleanupPathAction value;

    CleanupPathActionConfiguration(CleanupPathAction value) {
        this.value = value;
    }

    public CleanupPathAction value() {
        return value;
    }
}
