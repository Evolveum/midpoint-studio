package com.evolveum.midpoint.studio.impl.metrics;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum MetricsKey {

    SYSTEM_CPU_USAGE("system.cpu.usage", "System CPU Usage", MetricsCategory.SYSTEM),

    PROCESS_CPU_USAGE("process.cpu.usage", "Process CPU Usage", MetricsCategory.PROCESS),

    PROCESS_FILES_OPEN("process.files.open", "Process Files Open", MetricsCategory.PROCESS),

    JVM_GC_PAUSE("jvm.gc.pause", "JVM GC Pause", MetricsCategory.JVM),

    JVM_MEMORY_COMMITED("jvm.memory.committed", "JVM Memory Commited", MetricsCategory.JVM),

    JVM_MEMORY_USED("jvm.memory.used", "JVM Memory Used", MetricsCategory.JVM),

    JVM_THREADS_LIVE("jvm.threads.live", "JVM Threads Live", MetricsCategory.JVM),

    JVM_THREADS_PEAK("jvm.threads.peak", "JVM Threads Peak", MetricsCategory.JVM);

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
