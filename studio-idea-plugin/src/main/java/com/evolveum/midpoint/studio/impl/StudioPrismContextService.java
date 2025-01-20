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
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DOMUtilSettings;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
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
     * Default prism context initialized when class is loaded, will not change throughout the application lifecycle.
     */
    private static final PrismContext DEFAULT_PRISM_CONTEXT;

    static {
        LOG.info("Creating default prism context");
        try {
            DEFAULT_PRISM_CONTEXT = createPrismContext(null);
        } catch (Exception ex) {
            LOG.error("Couldn't initialize empty prism context", ex);

            throw new IllegalStateException("Couldn't initialize prism context", ex);
        }
        LOG.info("Default prism context created");

        LOG.info("Attaching PrismService override supplier");
        PrismService.overrideSupplier(() -> new PrismService.Mutable() {

            @Override
            public PrismContext prismContext() {
                ContextKey key = PRISM_SERVICE_PROJECT.get();
                if (key.projectId == null) {
                    if (!key.useDefaultPrismContext) {
                        LOG.warn(
                                "No project set for PrismService override supplier (empty thread local), " +
                                        "returning default prism context instance",
                                new RuntimeException("Exception just for stacktrace"));
                    }

                    return DEFAULT_PRISM_CONTEXT;
                }

                Project project = getProject(key.projectId);

                return StudioPrismContextService.getPrismContext(project);
            }

            @Override
            public void prismContext(PrismContext prismContext) {
                LOG.debug("PrismService override supplier: prism context set to " + prismContext);

                ContextKey key = PRISM_SERVICE_PROJECT.get();
                if (key.projectId == null) {
                    if (!key.useDefaultPrismContext) {
                        LOG.warn(
                                "No project set for PrismService override supplier (empty thread local), " +
                                        "ignoring prism context set");
                    }

                    return;
                }

                Project project = getProject(key.projectId);

                StudioPrismContextService.get(project).setPrismContext(prismContext);
            }
        });
    }

    private final @NotNull Project project;

    /**
     * Prism context specific for each project.
     */
    private PrismContext prismContext;

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
        LOG.info("Creating prism context for project: " + project.getName());

        var previousContext = prismContext != null ? prismContext : DEFAULT_PRISM_CONTEXT;
        try {
            PRISM_SERVICE_PROJECT.set(new ContextKey(this.project));

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            try {
                prismContext = createPrismContext(r -> registerExtensionSchemas(project, r));
                registerResourceSchemas(prismContext, () -> EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_RESOURCE));
                registerSchemaObjects(project, prismContext.getSchemaRegistry());

                prismContext.reload();
            } catch (Exception ex) {
                LOG.error("Couldn't initialize prism context", ex);

                prismContext = previousContext;
            } finally {
                Thread.currentThread().setContextClassLoader(cl);

                LOG.info("Prism context created for project: " + project.getName());
            }
        } finally {
            PRISM_SERVICE_PROJECT.remove();
        }

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
        try {
            PRISM_SERVICE_PROJECT.set(new ContextKey(project));

            runnable.run();
        } finally {
            PRISM_SERVICE_PROJECT.remove();
        }
    }

    public static <T> T runSupplierWithProject(@NotNull Project project, @NotNull Supplier<T> supplier) {
        try {
            PRISM_SERVICE_PROJECT.set(new ContextKey(project));

            return supplier.get();
        } finally {
            PRISM_SERVICE_PROJECT.remove();
        }
    }

    public static <T> T runCallableWithDefaultContext(@NotNull Callable<T> callable) throws Exception {
        try {
            PRISM_SERVICE_PROJECT.set(new ContextKey());

            return callable.call();
        } finally {
            PRISM_SERVICE_PROJECT.remove();
        }
    }

    public static <T> T runCallableWithProject(@NotNull Project project, @NotNull Callable<T> callable) throws Exception {
        try {
            PRISM_SERVICE_PROJECT.set(new ContextKey(project));

            return callable.call();
        } finally {
            PRISM_SERVICE_PROJECT.remove();
        }
    }

    private static Project getProject(@NotNull String projectId) {
        return ProjectManager.getInstance().findOpenProjectByHash(projectId);
    }

    /**
     * Key for prism context service thread local.
     *
     * @param projectId project identifier
     * @param useDefaultPrismContext whether to use default prism if project identifier is not set
     */
    private record ContextKey(String projectId, boolean useDefaultPrismContext) {

        public ContextKey(@NotNull String projectId) {
            this(projectId, false);
        }

        public ContextKey(@NotNull Project project) {
            this(project.getLocationHash(), false);
        }

        public ContextKey() {
            this(null, true);
        }
    }
}
