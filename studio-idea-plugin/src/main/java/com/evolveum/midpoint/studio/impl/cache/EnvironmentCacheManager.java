package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.client.TestConnectionResult;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LookupTableType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemConfigurationType;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.hints.ParameterHintsPassFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EnvironmentCacheManager {

    public static class CacheKey<C extends Cache> {

        public CacheKey() {
        }
    }

    public static final CacheKey<ObjectCache<LookupTableType>> KEY_LOOKUP_TABLE = new CacheKey<>();

    public static final CacheKey<ObjectCache<SchemaType>> KEY_SCHEMA = new CacheKey<>();

    public static final CacheKey<ObjectCache<SystemConfigurationType>> KEY_SYSTEM_CONFIGURATION = new CacheKey<>();

    public static final CacheKey<ConnectorCache> KEY_CONNECTOR = new CacheKey<>();

    public static final CacheKey<EnvironmentPropertiesCache> KEY_PROPERTIES = new CacheKey<>();

    public static final CacheKey<ExtensionSchemaCache> KEY_EXTENSION_SCHEMA = new CacheKey<>();

    public static final CacheKey<ResourceCache> KEY_RESOURCE = new CacheKey<>();

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentCacheManager.class);

    private final Project project;

    private final Map<CacheKey<?>, Cache> caches = new HashMap<>();

    private Environment environment;

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
        caches.put(KEY_LOOKUP_TABLE, new ObjectCache<>(project, LookupTableType.class));
        caches.put(KEY_SCHEMA, new ObjectCache<>(project, SchemaType.class));
        caches.put(KEY_SYSTEM_CONFIGURATION, new ObjectCache<>(project, SystemConfigurationType.class));
        caches.put(KEY_CONNECTOR, new ConnectorCache(project));
        caches.put(KEY_PROPERTIES, new EnvironmentPropertiesCache(project));
        caches.put(KEY_EXTENSION_SCHEMA, new ExtensionSchemaCache(project));
        caches.put(KEY_RESOURCE, new ResourceCache(project));
        MidPointService ms = MidPointService.get(project);
        MidPointConfiguration config = ms.getSettings();

        int cacheTTL = config.getCacheTTL();
        setCacheTTL(cacheTTL);

        EnvironmentService es = EnvironmentService.getInstance(project);
        setEnvironment(es.getSelected());

        MidPointUtils.subscribeToEnvironmentChange(project, e -> {
            LOG.info("Environment changed to {}, reloading caches", (e != null ? e.getName() : "null"));

            setEnvironment(e);

            reload(true, true);
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
    }

    public void setCacheTTL(int cacheTTL) {
        updateCachesConfiguration(c -> c.setTtl(cacheTTL));
    }

    /**
     * Reloads caches.
     *
     * @param forceClear     If true, caches will be cleared before attempt to reload.
     * @param testConnection If true, connection to the environment will be tested before reload.
     */
    public void reload(boolean forceClear, boolean testConnection) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            AppExecutorUtil.getAppExecutorService().submit(() -> reloadInternal(forceClear, testConnection));
        } else {
            reloadInternal(forceClear, testConnection);
        }
    }

    public void reloadInternal(boolean forceClear, boolean testConnection) {
        LOG.debug("Reloading caches (forceClear={}, testConnection={})", forceClear, testConnection);

        if (forceClear) {
            for (Cache cache : caches.values()) {
                cache.clear();
            }
        }

        if (testConnection) {
            MidPointClient client = new MidPointClient(project, environment, true, true);
            TestConnectionResult result = client.testConnection();
            LOG.debug("Test connection: {}", result.success() ? "success" : "failed");

            if (!result.success()) {
                LOG.debug("Skipping cache reload");
                restartHighlightInEditors();

                return;
            }
        }

        for (Cache cache : caches.values()) {
            try {
                cache.reload();
            } catch (Exception ex) {
                LOG.error("Error reloading cache: {}", ex.getMessage(), ex);
            }
        }

        restartHighlightInEditors();
//        StudioPrismContextService.get(project).resetPrismContext();

        LOG.debug("Caches reload finished");
    }

    private void restartHighlightInEditors() {
        // force re-highlight editors, this probably shouldn't be here, but right now no better place
        ParameterHintsPassFactory.forceHintsUpdateOnNextPass();
        DaemonCodeAnalyzer dca = DaemonCodeAnalyzer.getInstance(project);
        dca.restart();
    }
}