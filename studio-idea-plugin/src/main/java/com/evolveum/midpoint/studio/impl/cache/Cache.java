package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class Cache {

    /**
     * Default cache TTL 10 minutes.
     */
    public static final int DEFAULT_CACHE_TTL = 10 * 60;

    /**
     * Time to live in seconds.
     */
    private int ttl;

    private final Project project;

    private Environment environment;

    public Cache(@NotNull Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * @return time to live in seconds.
     */
    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl > 0 ? ttl : DEFAULT_CACHE_TTL;
    }

    abstract void clear();

    abstract void reload();
}
