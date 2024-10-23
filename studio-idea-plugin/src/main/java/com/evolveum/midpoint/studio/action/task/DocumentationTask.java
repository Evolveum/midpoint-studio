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
import com.evolveum.midscribe.generator.GenerateOptions;
import com.evolveum.midscribe.generator.Generator;
import com.evolveum.midscribe.generator.InMemoryObjectStore;
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

    private final GenerateOptions options;

    public DocumentationTask(
            @NotNull Project project, Supplier<DataContext> dataContextSupplier, @NotNull GenerateOptions options) {

        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);

        this.options = options;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        if (client != null) {
            try {
                MidPointObjectStore store = new MidPointObjectStore(options, midPointService, client);
                store.init();

                options.setObjectStoreInstance(store);
            } catch (Exception ex) {
                String msg = "Couldn't initialize object store for midscribe";
                LOG.warn(msg, ex);

                MidPointUtils.handleGenericException(getProject(), getEnvironment(), DocumentationTask.class, NOTIFICATION_KEY, msg, ex);
            }
        }

        Properties properties = new Properties();
        properties.put(RuntimeConstants.RESOURCE_MANAGER_INSTANCE, new ResourceManagerImpl());

        Generator generator = new Generator(options);
        generator.setLogListener(new MidscribeLogListener(getEnvironment(), midPointService));
        try {
            generator.generate(properties);
        } catch (Exception ex) {
            LOG.error("Couldn't generate documentation", ex);

            MidPointUtils.handleGenericException(getProject(), getEnvironment(), DocumentationTask.class,
                    NOTIFICATION_KEY, "Couldn't generate documentation", ex);
        }
    }

    private static class MidPointObjectStore extends InMemoryObjectStore {

        private Logger LOG = Logger.getInstance(MidPointObjectStore.class);

        private MidPointService midPointService;

        private MidPointClient client;

        public MidPointObjectStore(GenerateOptions options, MidPointService midPointService, MidPointClient client) {
            super(options);

            this.midPointService = midPointService;
            this.client = client;
        }

        @Override
        public PrismContext getPrismContext() {
            return client.getPrismContext();
        }

        @Override
        public <T extends ObjectType> List<T> list(Class<T> type) {
            List<T> objects = super.list(type);
            if (objects != null) {
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
        public <T extends ObjectType> T get(Class<T> type, String oid) {
            T object = super.get(type, oid);
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
                MidPointObject obj = client.get(type, oid, new SearchOptions().raw(true));
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
