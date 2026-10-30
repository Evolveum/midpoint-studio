package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.BackgroundReload;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.VisibleForTesting;

import java.time.Duration;
import org.jetbrains.annotations.NotNull;

public abstract class Cache {

    /**
     * Default cache TTL 10 minutes.
     */
    public static final int DEFAULT_CACHE_TTL = 10 * 60;

    /**
     * Time to live in seconds.
     */
    private int ttl = DEFAULT_CACHE_TTL;

    private final Project project;

    private Environment environment;

    /**
     * Background reload of this cache. Readers never load on their thread, they answer from what
     * is cached and request a reload when it is stale, see {@link #refreshIfStale()}.
     */
    private final BackgroundReload reloader;

    private volatile long lastReloadTime;

    public Cache(@NotNull Project project) {
        this.project = project;
        this.reloader = BackgroundReload.forProject(project, getClass().getSimpleName(), this::reload);
    }

    public Cache(@NotNull Project project, @NotNull String reloadName) {
        this.project = project;
        this.reloader = BackgroundReload.forProject(project, reloadName, this::reload);
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

    /**
     * Requests one background reload when the cached data is older than the TTL and no reload
     * runs. Never waits, the caller keeps what is cached (possibly nothing) until the reload lands.
     */
    protected void refreshIfStale() {
        if (isStale() && !reloader.isRunning()) {
            reloader.request();
        }
    }

    protected boolean isStale() {
        return System.currentTimeMillis() - lastReloadTime > getTtl() * 1000L;
    }

    protected void markReloaded() {
        lastReloadTime = System.currentTimeMillis();
    }

    public void requestReload() {
        reloader.request();
    }

    public boolean isReloading() {
        return reloader.isRunning();
    }

    @VisibleForTesting
    void waitForReload() throws Exception {
        reloader.waitForIdle(Duration.ofSeconds(60));
    }

    @VisibleForTesting
    void expireForTests() {
        lastReloadTime = 0;
    }

    abstract void clear();

    /**
     * Loads the data, called on a background thread with the project bound. Must not be called
     * from readers.
     */
    abstract void reload();
}
