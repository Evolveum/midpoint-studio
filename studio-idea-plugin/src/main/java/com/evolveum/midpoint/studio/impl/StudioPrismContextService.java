package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismService;
import com.evolveum.midpoint.prism.impl.schema.SchemaRegistryImpl;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.studio.client.ServiceFactory;
import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.cache.ExtensionSchemaCache;
import com.evolveum.midpoint.studio.impl.cache.ObjectCache;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DOMUtilSettings;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class StudioPrismContextService {

    private static final Logger LOG = Logger.getInstance(StudioPrismContextService.class);

    private final Project project;

    public StudioPrismContextService(Project project) {
        this.project = project;
    }

    public static StudioPrismContextService get(@NotNull Project project) {
        return new StudioPrismContextService(project);
    }

    public synchronized void resetPrismContext() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(ServiceFactory.class.getClassLoader());

            // just to initialize MiscUtil class with correct classloader
            MiscUtil.emptyIfNull("");

            DOMUtilSettings.setAddTransformerFactorySystemProperty(false);

            MidPointPrismContextFactory factory = new MidPointPrismContextFactory() {

                @Override
                protected void registerExtensionSchemas(SchemaRegistryImpl schemaRegistry) throws SchemaException, IOException {
                    super.registerExtensionSchemas(schemaRegistry);

                    registerSchemaObjects(schemaRegistry);

                    registerSchemaFiles(schemaRegistry);
                }
            };

            PrismContext prismContext = factory.createPrismContext();
            prismContext.initialize();

            PrismService prismService = PrismService.get();
            prismService.prismContext(prismContext);
        } catch (Exception ex) {
            throw new IllegalStateException("Couldn't initialize prism context", ex);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private void registerSchemaObjects(SchemaRegistryImpl schemaRegistry) {
        ObjectCache<SchemaType> cache = EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_SCHEMA);

        for (SchemaType schema : cache.list()) {
            String description = "extension schema object '" + schema.getNamespace() + "'";

            Element element = schema.getDefinition().getSchema();
            String content = DOMUtil.serializeDOMToString(element);

            registerExtensionSchema(schemaRegistry, content, description);
        }
    }

    private void registerSchemaFiles(SchemaRegistryImpl schemaRegistry) {
        ExtensionSchemaCache cache = EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_EXTENSION_SCHEMA);

        for (Map.Entry<String, XmlFile> entry : cache.getFiles().entrySet()) {
            String description = "extension schema file '" + entry.getKey() + "'";
            String content = entry.getValue().getText();

            registerExtensionSchema(schemaRegistry, content, description);
        }
    }

    private void registerExtensionSchema(SchemaRegistryImpl registry, String content, String description) {
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            registry.registerPrismSchema(is, description);
        } catch (Exception ex) {
            LOG.debug("Couldn't register " + description, ex);
        }
    }
}
