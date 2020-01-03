package com.evolveum.midpoint.studio.impl.metrics;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum MetricsCategory {

    SYSTEM("System"),

    JVM("JVM");

    private String displayName;

    MetricsCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
