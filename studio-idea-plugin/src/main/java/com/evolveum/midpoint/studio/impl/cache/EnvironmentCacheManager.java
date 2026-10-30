package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.client.TestConnectionResult;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LookupTableType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemConfigurationType;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.hints.ParameterHintsPassFactory;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EnvironmentCacheManager {

    public static class CacheKey<C extends Cache> {

        private final String name;

        public CacheKey(@NotNull String name) {
            this.name = name;
        }

        /**
         * Shown as the step text in the reload progress.
         */
        public @NotNull String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final CacheKey<ObjectCache<LookupTableType>> KEY_LOOKUP_TABLE = new CacheKey<>("Lookup tables");

    public static final CacheKey<ObjectCache<SchemaType>> KEY_SCHEMA = new CacheKey<>("Schema objects");

    public static final CacheKey<ObjectCache<SystemConfigurationType>> KEY_SYSTEM_CONFIGURATION = new CacheKey<>("System configuration");

    public static final CacheKey<ConnectorCache> KEY_CONNECTOR = new CacheKey<>("Connectors");

    public static final CacheKey<EnvironmentPropertiesCache> KEY_PROPERTIES = new CacheKey<>("Environment properties");

    public static final CacheKey<ExtensionSchemaCache> KEY_EXTENSION_SCHEMA = new CacheKey<>("Extension schema files");

    public static final CacheKey<ObjectCache<ResourceType>> KEY_RESOURCE = new CacheKey<>("Resources");

    public static final CacheKey<InitialObjectsCache> KEY_INITIAL_OBJECTS = new CacheKey<>("Initial objects");

    /**
     * Reload order, also the order of the progress steps.
     */
    private static final List<CacheKey<?>> KEYS = List.of(
            KEY_LOOKUP_TABLE,
            KEY_SCHEMA,
            KEY_SYSTEM_CONFIGURATION,
            KEY_CONNECTOR,
            KEY_PROPERTIES,
            KEY_EXTENSION_SCHEMA,
            KEY_RESOURCE,
            KEY_INITIAL_OBJECTS);

    public static @NotNull List<CacheKey<?>> cacheKeys() {
        return KEYS;
    }

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentCacheManager.class);

    private final Project project;

    private final Map<CacheKey<?>, Cache> caches = new LinkedHashMap<>();

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
        caches.put(KEY_RESOURCE, new ObjectCache<>(project, ResourceType.class));
        caches.put(KEY_INITIAL_OBJECTS, new InitialObjectsCache(project));
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
     * Runs as a cancellable background task "Reloading <environment>" with one step per cache
     * and a last step that waits for the prism context build, so the status bar shows what is
     * being refreshed. Cancelling stops at the next step, a running HTTP call is not interrupted.
     *
     * @param forceClear     If true, caches will be cleared before attempt to reload.
     * @param testConnection If true, connection to the environment will be tested before reload.
     */
    public void reload(boolean forceClear, boolean testConnection) {
        String title = "Reloading " + (environment != null ? environment.getName() : "environment");

        ProgressManager.getInstance().run(new Task.Backgroundable(project, title, true) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                reloadInternal(forceClear, testConnection, indicator);
            }
        });
    }

    public void reloadInternal(boolean forceClear, boolean testConnection) {
        reloadInternal(forceClear, testConnection, new EmptyProgressIndicator());
    }

    public void reloadInternal(boolean forceClear, boolean testConnection, @NotNull ProgressIndicator indicator) {
        String environmentName = environment != null ? environment.getName() : "none";
        long start = System.currentTimeMillis();
        LOG.info("Reloading environment {} (forceClear={}, testConnection={})", environmentName, forceClear, testConnection);

        int steps = (testConnection ? 1 : 0) + caches.size() + 1;
        int step = 0;
        indicator.setIndeterminate(false);

        if (forceClear) {
            for (Cache cache : caches.values()) {
                cache.clear();
            }
        }

        if (testConnection) {
            indicator.setText("Testing connection");
            indicator.setFraction((double) step++ / steps);

            MidPointClient client = new MidPointClient(project, environment, true, true);
            TestConnectionResult result = StudioPrismContextService.runSupplierWithProject(project, client::testConnection);
            LOG.info("Test connection to {}: {}", environmentName, result.success() ? "success" : "failed");

            if (!result.success()) {
                LOG.info("Skipping cache reload for {}, connection test failed", environmentName);
                restartHighlightInEditors();

                return;
            }
        }

        for (Map.Entry<CacheKey<?>, Cache> entry : caches.entrySet()) {
            indicator.checkCanceled();

            // raw: the event publisher is generic over the cache type
            CacheKey key = entry.getKey();
            Cache cache = entry.getValue();

            indicator.setText(key.getName());
            indicator.setFraction((double) step++ / steps);
            try {
                long cacheStart = System.currentTimeMillis();
                StudioPrismContextService.runWithProject(project, cache::reload);
                LOG.info("Reloaded {} for {} in {} ms", key.getName(), environmentName, System.currentTimeMillis() - cacheStart);

                project.getMessageBus().syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC).environmentCacheReloaded(key, cache);
            } catch (Exception ex) {
                RunnableUtils.rethrowIfCancellation(ex);

                LOG.error("Error reloading {} for {}: {}", key.getName(), environmentName, ex.getMessage(), ex);
            }
        }

        restartHighlightInEditors();

        project.getMessageBus().syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC).environmentCacheManagerReloaded();

        indicator.setText("Prism context");
        indicator.setFraction((double) step / steps);
        LOG.info("Waiting for the prism context rebuild for {}", environmentName);
        StudioPrismContextService.get(project).awaitReload(indicator);

        LOG.info("Environment {} reloaded in {} ms", environmentName, System.currentTimeMillis() - start);
    }

    private void restartHighlightInEditors() {
        // force re-highlight editors, this probably shouldn't be here, but right now no better place
        ParameterHintsPassFactory.forceHintsUpdateOnNextPass();
        DaemonCodeAnalyzer dca = DaemonCodeAnalyzer.getInstance(project);
        dca.restart();
    }
}