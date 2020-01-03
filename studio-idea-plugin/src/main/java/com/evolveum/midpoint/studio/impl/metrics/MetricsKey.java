package com.evolveum.midpoint.studio.impl.metrics;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum MetricsKey {

    CPU("Cpu", MetricsCategory.SYSTEM),

    MEMORY("Memory", MetricsCategory.SYSTEM);

    private String displayName;

    private MetricsCategory category;

    MetricsKey(String displayName, MetricsCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MetricsCategory getCategory() {
        return category;
    }
}
