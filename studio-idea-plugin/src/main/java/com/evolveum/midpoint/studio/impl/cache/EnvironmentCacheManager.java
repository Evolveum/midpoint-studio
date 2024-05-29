package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.client.TestConnectionResult;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EnvironmentCacheManager {

    public static class CacheKey<C extends Cache> {

        private final Class cacheType;

        public CacheKey(Class cacheType) {
            this.cacheType = cacheType;
        }
    }

    public static final CacheKey<ObjectCache<SchemaType>> KEY_SCHEMA = new CacheKey<>(ObjectCache.class);
    public static final CacheKey<ObjectCache<SystemConfigurationType>> KEY_SYSTEM_CONFIGURATION = new CacheKey<>(ObjectCache.class);
    public static final CacheKey<ConnectorCache> KEY_CONNECTOR = new CacheKey<>(ConnectorCache.class);
    public static final CacheKey<EnvironmentPropertiesCache> KEY_PROPERTIES = new CacheKey<>(EnvironmentPropertiesCache.class);

    private static class ManagerState {

        boolean environmentAvailable;

        boolean showNotification;
    }

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentCacheManager.class);

    private final Project project;

    private final Map<CacheKey<?>, Cache> caches = new HashMap<>();

    private Environment environment;

    private ManagerState state;

    public EnvironmentCacheManager(Project project) {
        this.project = project;

        init();
    }

    public static EnvironmentCacheManager get(Project project) {
        return project.getService(EnvironmentCacheManager.class);
    }

    public static <C extends Cache> C getCache(Project project, CacheKey<C> cache) {
        return get(project).getCache(cache);
    }

    private void init() {
        caches.put(KEY_SCHEMA, new ObjectCache<>(project, SchemaType.class));
        caches.put(KEY_SYSTEM_CONFIGURATION, new ObjectCache<>(project, SystemConfigurationType.class));
        caches.put(KEY_CONNECTOR, new ConnectorCache(project));
        caches.put(KEY_PROPERTIES, new EnvironmentPropertiesCache(project));

        MidPointService ms = MidPointService.get(project);
        MidPointConfiguration config = ms.getSettings();

        long cacheTTL = config.getCacheTTL();
        setCacheTTL(cacheTTL);

        EnvironmentService es = EnvironmentService.getInstance(project);
        setEnvironment(es.getSelected());

        reload();

        MidPointUtils.subscribeToEnvironmentChange(project, e -> {
            LOG.info(
                    "Environment changed to {}, reloading cache for {}",
                    (e != null ? e.getName() : "null"),
                    getClass().getSimpleName());

            setEnvironment(e);

            reload();
        });
    }

    private <C extends Cache> void updateCachesConfiguration(Consumer<C> consumer) {
        caches.values().forEach(c -> consumer.accept((C) c));
    }

    public <C extends Cache> C getCache(CacheKey<C> cache) {
        return (C) caches.get(cache);
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;

        updateCachesConfiguration(c -> c.setEnvironment(environment));

        state = new ManagerState();
    }

    public void setCacheTTL(long cacheTTL) {
        updateCachesConfiguration(c -> c.setTtl(cacheTTL));
    }

    public void reload() {
        AppExecutorUtil.getAppExecutorService().submit(() -> reloadInternal());
    }

    public void reloadInternal() {
        try {
            Thread.sleep(10000L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        MidPointClient client = new MidPointClient(project, environment, true, true);
        TestConnectionResult testConnection = client.testConnection();
        LOG.debug("Test connection: {}", testConnection.success() ? "success" : "failed");

        if (!testConnection.success()) {
            state.environmentAvailable = false;

//            if (state.showNotification) {
//                state.showNotification = false;
//
//                MidPointUtils.publishNotification(
//                        project,
//                        "Environment is not available",
//                        "Environment is not available, configuration couldn't be cached");
//            }

            LOG.debug("Skipping cache reload");
            return;
        } else {
            state.environmentAvailable = true;
        }

        LOG.debug("Reloading caches");

        for (Cache cache : caches.values()) {
            try {
                cache.reload();
            } catch (Exception ex) {
                LOG.error("Error reloading cache: {}", ex.getMessage(), ex);
            }
        }
    }
}