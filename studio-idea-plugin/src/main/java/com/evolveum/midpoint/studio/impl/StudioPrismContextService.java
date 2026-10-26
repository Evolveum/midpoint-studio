package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismService;
import com.evolveum.midpoint.prism.impl.schema.SchemaRegistryImpl;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.schema.processor.ResourceSchemaRegistry;
import com.evolveum.midpoint.schema.processor.ValueBasedDefinitionLookupsImpl;
import com.evolveum.midpoint.studio.client.ServiceFactory;
import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.cache.ExtensionSchemaCache;
import com.evolveum.midpoint.studio.impl.cache.ObjectCache;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DOMUtilSettings;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Service that provides prism context for studio project.
 * <p>
 * Also takes into account extension schemas and schema objects and resources loaded from {@link EnvironmentCacheManager}.
 */
@SuppressWarnings("unused")
@Service(Service.Level.PROJECT)
public final class StudioPrismContextService implements ProjectManagerListener {

    private static final Logger LOG = Logger.getInstance(StudioPrismContextService.class);

    /**
     * ContextKey allows two different scenarios:
     * 1. Project is set, prism context is created for that project.
     * 2. Project is not set, default prism context is used (and no warning if explicitly asked for default prism context).
     */
    private static final ThreadLocal<ContextKey> PRISM_SERVICE_PROJECT = new ThreadLocal<>();

    /**
     * Number of times the supplier fell back to the default context because no project was bound
     * to the current thread. The fallback is a normal case for UI/PSI threads, so it is not
     * logged per occurrence. A flood of fallbacks usually means a code path that should run
     * with a project context (run*WithProject wrapper) doesn't.
     */
    private static final AtomicLong DEFAULT_CONTEXT_FALLBACK_COUNT = new AtomicLong();

    @VisibleForTesting
    static final long FALLBACK_LOG_INTERVAL = 1000;

    /**
     * Prism service routing context access to the project bound to the current thread,
     * or to the default context when no project is bound.
     */
    private static final PrismService.Mutable PRISM_SERVICE = new PrismService.Mutable() {

        @Override
        public PrismContext prismContext() {
            ContextKey key = PRISM_SERVICE_PROJECT.get();

            if (key == null || key.project == null) {
                if (key == null || !key.useDefaultPrismContext) {
                    reportDefaultContextFallback();
                }
                return DefaultPrismContext.getInstance().getUnlessBuilding();
            }

            Project project = key.project;
            if (project.isDisposed()) {
                LOG.debug("Project " + project.getName() + " is disposed, returning default prism context instance");
                return getDefaultPrismContext();
            }

            return StudioPrismContextService.getPrismContext(project);
        }

        @Override
        public void prismContext(PrismContext prismContext) {
            LOG.debug("PrismService override supplier: prism context set to " + prismContext);

            if (DefaultPrismContext.getInstance().acceptUnderConstruction(prismContext)) {
                return;
            }

            ContextKey key = PRISM_SERVICE_PROJECT.get();
            if (key == null || key.project == null) {
                LOG.debug(
                        "No project set for PrismService override supplier (empty thread local), " +
                                "ignoring prism context set");
                return;
            }

            Project project = key.project;
            if (project.isDisposed()) {
                LOG.debug("Project " + project.getName() + " is disposed, ignoring prism context set");
                return;
            }

            StudioPrismContextService.get(project).setPrismContext(prismContext);
        }
    };

    static {
        // cheap and must happen before any prism context is built, the default context itself
        // is built lazily in the background by DefaultPrismContext
        LOG.info("Attaching PrismService override supplier");
        PrismService.overrideSupplier(() -> PRISM_SERVICE);
    }

    /**
     * Default prism context (no project, no extension schemas). Waits for the background build
     * on first access, see {@link DefaultPrismContext#get()}.
     */
    public static @NotNull PrismContext getDefaultPrismContext() {
        return DefaultPrismContext.getInstance().get();
    }

    static PrismContext createDefaultPrismContext() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            return runCallableWithDefaultContext(() -> createPrismContext(null));
        } catch (Exception ex) {
            throw new IllegalStateException("Couldn't create default prism context: " + ex.getMessage(), ex);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private final @NotNull Project project;

    /**
     * Prism context specific for each project.
     */
    private PrismContext prismContext;

    private final AtomicLong reloadCount = new AtomicLong();

    private final AtomicLong totalReloadTimeMs = new AtomicLong();

    @SuppressWarnings("unused")
    public StudioPrismContextService(@NotNull Project project) {
        this.project = project;

        initialize();
    }

    public static @NotNull StudioPrismContextService get(@NotNull Project project) {
        return project.getService(StudioPrismContextService.class);
    }

    private void initialize() {
        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifier() {

            @Override
            public void environmentCacheManagerReloaded() {
                prismContext = reloadPrismContext();
            }
        });
    }

    public static @NotNull PrismContext getPrismContext(@NotNull Project project) {
        return project.getService(StudioPrismContextService.class).getPrismContext();
    }

    public synchronized @NotNull PrismContext getPrismContext() {
        if (prismContext == null) {
            reloadPrismContext();
        }
        return prismContext;
    }

    private synchronized void setPrismContext(@NotNull PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    private synchronized PrismContext reloadPrismContext() {
        long now = System.currentTimeMillis();

        LOG.info("Creating prism context for project: " + project.getName());

        var previousContext = prismContext != null ? prismContext : getDefaultPrismContext();

        ContextKey previousKey = bindKey(new ContextKey(this.project));
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            try {
                prismContext = createPrismContext(r -> registerExtensionSchemas(project, r));
                registerResourceSchemas(prismContext, () -> EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_RESOURCE));
                registerSchemaObjects(project, prismContext.getSchemaRegistry());

                prismContext.reload();
            } catch (Exception ex) {
                // cancellation (e.g. indexer read action cancelled by a write action) must propagate,
                // and prismContext must stay as it was: on first load it stays null so the next
                // access retries instead of being pinned to the default context
                RunnableUtils.rethrowIfCancellation(ex);

                LOG.error("Couldn't initialize prism context", ex);

                prismContext = previousContext;
            } finally {
                Thread.currentThread().setContextClassLoader(cl);

                LOG.info("Prism context created for project: " + project.getName());
            }
        } finally {
            restoreKey(previousKey);
        }

        long elapsed = System.currentTimeMillis() - now;
        long count = reloadCount.incrementAndGet();
        long totalMs = totalReloadTimeMs.addAndGet(elapsed);

        LOG.info("Prism context reload time: " + elapsed + " ms (reload count: " + count + ", total reload time: " + totalMs + " ms)");

        return prismContext;
    }

    private static PrismContext createPrismContext(
            Consumer<SchemaRegistryImpl> extensionsConsumer)
            throws SchemaException, IOException, SAXException {

        Thread.currentThread().setContextClassLoader(ServiceFactory.class.getClassLoader());

        // HACK: just to initialize MiscUtil class with correct classloader
        MiscUtil.emptyIfNull("");

        DOMUtilSettings.setAddTransformerFactorySystemProperty(false);

        MidPointPrismContextFactory factory = new MidPointPrismContextFactory() {

            @Override
            protected void registerExtensionSchemas(SchemaRegistryImpl schemaRegistry) throws SchemaException, IOException {
                super.registerExtensionSchemas(schemaRegistry);

                if (extensionsConsumer != null) {
                    extensionsConsumer.accept(schemaRegistry);
                }
            }
        };

        PrismContext context = factory.createPrismContext();
        context.initialize();
        return context;
    }

    private static void registerResourceSchemas(PrismContext context, Supplier<ObjectCache<ResourceType>> resourceCacheSupplier) {
        if (resourceCacheSupplier != null) {
            ResourceSchemaRegistry resourceSchemaRegistry = new ResourceSchemaRegistry();
            context.getDefaultSchemaLookup().registerSchemaSpecific(
                    ResourceSchemaRegistry.class, (s) -> resourceSchemaRegistry);

            resourceSchemaRegistry.registerResourceObjectLoader(oid -> {
                ObjectCache<ResourceType> cache = resourceCacheSupplier.get();
                ResourceType resource = cache.get(oid);

                return resource != null ? resource.asPrismObject() : null;
            });

            var valueBased = new ValueBasedDefinitionLookupsImpl();
            valueBased.setResourceSchemaRegistry(resourceSchemaRegistry);
            valueBased.init(context);
        }
    }

    private void registerExtensionSchemas(Project project, SchemaRegistryImpl schemaRegistry) {
        LOG.info("Registering extension schemas for project: " + project.getName());
        registerSchemaFiles(project, schemaRegistry);
    }

    private void registerSchemaObjects(Project project, SchemaRegistry schemaRegistry) {
        ObjectCache<SchemaType> cache = EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_SCHEMA);

        Map<String, Element> schemas = cache.list().stream()
                .collect(
                        Collectors.toMap(
                                s -> "extension schema object '" + s.getName() + "'",
                                s -> s.getDefinition().getSchema())
                );

        LOG.info("Registering %s schema objects".formatted(schemas.size()));

        try {
            schemaRegistry.registerDynamicSchemaExtensions(schemas);
        } catch (Exception ex) {
            LOG.debug("Couldn't register schema objects", ex);
        }
    }

    private void registerSchemaFiles(Project project, SchemaRegistryImpl schemaRegistry) {
        ExtensionSchemaCache cache = EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_EXTENSION_SCHEMA);

        Map<String, Element> schemas = cache.getFiles().entrySet().stream()
                .collect(
                        Collectors.toMap(
                                e -> "extension schema object '" + e.getKey() + "'",
                                e -> DOMUtil.parseDocument(e.getValue().getText()).getDocumentElement())
                );

        LOG.info("Registering %s schema files".formatted(schemas.size()));

        try {
            schemaRegistry.registerDynamicSchemaExtensions(schemas);
        } catch (Exception ex) {
            LOG.debug("Couldn't register schema files", ex);
        }
    }

    public static void runWithProject(@NotNull Project project, @NotNull Runnable runnable) {
        ContextKey previousKey = bindKey(new ContextKey(project));
        try {
            runnable.run();
        } finally {
            restoreKey(previousKey);
        }
    }

    public static <T> T runSupplierWithProject(@NotNull Project project, @NotNull Supplier<T> supplier) {
        ContextKey previousKey = bindKey(new ContextKey(project));
        try {
            return supplier.get();
        } finally {
            restoreKey(previousKey);
        }
    }

    public static <T> T runCallableWithDefaultContext(@NotNull Callable<T> callable) throws Exception {
        ContextKey previousKey = bindKey(new ContextKey());
        try {
            return callable.call();
        } finally {
            restoreKey(previousKey);
        }
    }

    public static <T> T runCallableWithProject(@NotNull Project project, @NotNull Callable<T> callable) throws Exception {
        ContextKey previousKey = bindKey(new ContextKey(project));
        try {
            return callable.call();
        } finally {
            restoreKey(previousKey);
        }
    }

    /**
     * Binds the key to the current thread and returns the previously bound one (may be null).
     * Wrappers nest (e.g. InitialObjectsCache.createReloadQuery inside EnvironmentCacheManager.reload,
     * or a context reload triggered from the supplier), so the previous key must be restored
     * with {@link #restoreKey(ContextKey)} instead of removing the thread local unconditionally.
     * Otherwise the outer context is lost for the rest of the outer wrapper's execution.
     */
    private static ContextKey bindKey(@NotNull ContextKey key) {
        ContextKey previous = PRISM_SERVICE_PROJECT.get();
        PRISM_SERVICE_PROJECT.set(key);

        return previous;
    }

    private static void restoreKey(ContextKey previous) {
        if (previous != null) {
            PRISM_SERVICE_PROJECT.set(previous);
        } else {
            PRISM_SERVICE_PROJECT.remove();
        }
    }

    /**
     * Counts a fallback to the default context and logs a warning with a stack trace of the
     * caller when the fallback happens too often.
     */
    @VisibleForTesting
    static void reportDefaultContextFallback() {
        long count = DEFAULT_CONTEXT_FALLBACK_COUNT.incrementAndGet();

        if (count >= FALLBACK_LOG_INTERVAL && DEFAULT_CONTEXT_FALLBACK_COUNT.compareAndSet(count, 0)) {
            LOG.warn(
                    "No project set for PrismService override supplier (empty thread local), " +
                            "returning default prism context instance. This has happened " + count +
                            " times since the last report, the call site is likely missing a run*WithProject wrapper:",
                    new Exception());
        }
    }

    @VisibleForTesting
    static long getDefaultContextFallbackCount() {
        return DEFAULT_CONTEXT_FALLBACK_COUNT.get();
    }

    @VisibleForTesting
    static void resetDefaultContextFallbackCount() {
        DEFAULT_CONTEXT_FALLBACK_COUNT.set(0);
    }

    /**
     * Key for prism context service thread local.
     */
    private record ContextKey(Project project, boolean useDefaultPrismContext) {

        public ContextKey(@NotNull Project project) {
            this(project, false);
        }

        public ContextKey() {
            this(null, true);
        }
    }
}
