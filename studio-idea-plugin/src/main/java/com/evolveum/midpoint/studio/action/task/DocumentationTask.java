package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.SearchResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.MidscribeLogListener;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midscribe.generator.Generator;
import com.evolveum.midscribe.generator.GeneratorOptions;
import com.evolveum.midscribe.generator.ObjectStoreFactory;
import com.evolveum.midscribe.generator.store.GetOptions;
import com.evolveum.midscribe.generator.store.InMemoryObjectStoreFactory;
import com.evolveum.midscribe.generator.store.ObjectStore;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DocumentationTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(DocumentationTask.class);

    public static String TITLE = "Documentation task";

    public static String NOTIFICATION_KEY = TITLE;

    private final GeneratorOptions options;

    public DocumentationTask(
            @NotNull Project project, Supplier<DataContext> dataContextSupplier, @NotNull GeneratorOptions options) {

        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);

        this.options = options;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);
        Properties properties = new Properties();
        properties.put(RuntimeConstants.RESOURCE_MANAGER_INSTANCE, new ResourceManagerImpl());

        Generator generator = new Generator(options);
        generator.setLogListener(new MidscribeLogListener(getEnvironment(), midPointService));
        generator.setObjectStoreFactory(new StudioObjectStoreFactory(midPointService, client));
        try {
            generator.generate(properties);
        } catch (Exception ex) {
            LOG.error("Couldn't generate documentation", ex);

            MidPointUtils.handleGenericException(getProject(), getEnvironment(), DocumentationTask.class,
                    NOTIFICATION_KEY, "Couldn't generate documentation", ex);
        }
    }

    private static class StudioObjectStoreFactory implements ObjectStoreFactory {

        private final MidPointService midPointService;

        private final MidPointClient client;

        public StudioObjectStoreFactory(MidPointService midPointService, MidPointClient client) {
            this.midPointService = midPointService;
            this.client = client;
        }

        @Override
        public ObjectStore createObjectStore(GeneratorOptions options, PrismContext prismContext, boolean additional) {
            ObjectStore store = new InMemoryObjectStoreFactory().createObjectStore(options, prismContext, additional);

            return new StudioObjectStore(store, midPointService, client);
        }
    }

    private static class StudioObjectStore implements ObjectStore {

        private Logger LOG = Logger.getInstance(StudioObjectStore.class);

        private ObjectStore store;

        private MidPointService midPointService;

        private MidPointClient client;

        public StudioObjectStore(ObjectStore store, MidPointService midPointService, MidPointClient client) {
            this.store = store;
            this.midPointService = midPointService;
            this.client = client;
        }

        @Override
        public <T extends ObjectType> List<T> list(Class<T> type, GetOptions options) {
            List<T> objects = store.list(type, options);
            if (!objects.isEmpty()) {
                return objects;
            }

            if (!type.isAssignableFrom(ConnectorType.class)) {
                return Collections.emptyList();
            }

            if (client == null) {
                return Collections.emptyList();
            }

            SearchResult result = client.search(ConnectorType.class, null, true);
            if (result == null) {
                return Collections.emptyList();
            }

            return (List) result.getObjects().stream().map(o -> parseObject(o)).filter(o -> o != null).collect(Collectors.toList());
        }

        @Override
        public <T extends ObjectType> T get(Class<T> type, String oid, GetOptions options) {
            T object = store.get(type, oid, options);
            if (object != null) {
                return object;
            }

            if (!type.isAssignableFrom(ConnectorType.class)) {
                return null;
            }

            if (client == null) {
                return null;
            }

            try {
                com.evolveum.midpoint.studio.client.MidPointObject obj = client.get(type, oid, new SearchOptions().raw(true));
                return parseObject(obj);
            } catch (Exception ex) {
                logException("Couldn't get object with oid " + oid, ex);
            }

            return null;
        }

        private <T extends ObjectType> T parseObject(MidPointObject obj) {
            if (obj == null) {
                return null;
            }

            try {
                return (T) client.parseObject(obj.getContent()).asObjectable();
            } catch (Exception ex) {
                logException("Couldn't parse object with oid " + obj.getOid(), ex);
            }

            return null;
        }

        private void logException(String msg, Exception ex) {
            LOG.warn(msg, ex);

            if (midPointService != null) {
                midPointService.printToConsole(client.getEnvironment(), DocumentationTask.class, msg, ex);
            }
        }
    }
}
