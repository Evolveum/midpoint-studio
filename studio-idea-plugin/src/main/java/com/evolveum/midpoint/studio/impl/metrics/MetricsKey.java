package com.evolveum.midpoint.studio.impl.metrics;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum MetricsKey {

    SYSTEM_CPU_USAGE("system.cpu.usage", "CPU Usage", MetricsCategory.SYSTEM),

    PROCESS_CPU_USAGE("process.cpu.usage", "CPU Usage", MetricsCategory.PROCESS),

    PROCESS_FILES_OPEN("process.files.open", "Files Open", MetricsCategory.PROCESS),

    JVM_GC_PAUSE("jvm.gc.pause", "GC Pause", MetricsCategory.JVM),

    JVM_MEMORY_COMMITED("jvm.memory.committed", "Memory Commited", MetricsCategory.JVM),

    JVM_MEMORY_USED("jvm.memory.used", "Memory Used", MetricsCategory.JVM),

    JVM_THREADS_LIVE("jvm.threads.live", "Threads Live", MetricsCategory.JVM),

    JVM_THREADS_PEAK("jvm.threads.peak", "Threads Peak", MetricsCategory.JVM);

    private String key;

    private String displayName;

    private MetricsCategory category;

    MetricsKey(String key, String displayName, MetricsCategory category) {
        this.key = key;
        this.displayName = displayName;
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MetricsCategory getCategory() {
        return category;
    }
}
