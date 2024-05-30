package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.project.Project;

public abstract class Cache {

    /**
     * Default cache TTL 10 minutes.
     */
    public static final long DEFAULT_CACHE_TTL = 10 * 60;

    /**
     * Time to live in seconds.
     */
    private long ttl;

    private final Project project;

    private Environment environment;

    public Cache(Project project) {
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
    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl > 0 ? ttl : DEFAULT_CACHE_TTL;
    }

    abstract void clear();

    abstract void reload();
}
