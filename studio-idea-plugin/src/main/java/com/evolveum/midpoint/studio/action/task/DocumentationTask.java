package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.SearchResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.evolveum.midscribe.generator.Generator;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DocumentationTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(DocumentationTask.class);

    public static String TITLE = "Documentation task";

    public static String NOTIFICATION_KEY = TITLE;

    private GenerateOptions options;

    public DocumentationTask(@NotNull AnActionEvent event, @NotNull GenerateOptions options) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);

        this.options = options;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        // todo finish
//        if (client != null) {
//            options.setMidpointClient(new MidscribeClient(client));
//        }

        Properties properties = new Properties();
        properties.put(RuntimeConstants.RESOURCE_MANAGER_INSTANCE, new ResourceManagerImpl());

        Generator generator = new Generator(options);
        try {
            generator.generate(properties);
        } catch (Exception ex) {
            LOG.error("Couldn't generate documentation", ex);

            MidPointUtils.handleGenericException(getProject(), getEnvironment(), DocumentationTask.class,
                    NOTIFICATION_KEY, "Couldn't generate documentation", ex);
        }
    }

    private static class MidscribeClient implements com.evolveum.midscribe.generator.MidPointClient {

        private MidPointClient client;

        public MidscribeClient(MidPointClient client) {
            this.client = client;
        }

        @Override
        public void init() throws Exception {
        }

        @Override
        public void destroy() throws Exception {
        }

        @Override
        public PrismContext getPrismContext() {
            return client.getPrismContext();
        }

        @Override
        public <T extends ObjectType> List<T> list(Class<T> type) {
            SearchResult result = client.search(type, null, true);
            if (result == null) {
                return Collections.emptyList();
            }

            return (List) result.getObjects().stream().map(o -> parseObject(o)).filter(o -> o != null).collect(Collectors.toList());
        }

        @Override
        public <T extends ObjectType> T get(Class<T> type, String oid) {
            MidPointObject obj = client.get(type, oid, new SearchOptions().raw(true));

            return parseObject(obj);
        }

        private <T extends ObjectType> T parseObject(MidPointObject obj) {
            if (obj == null) {
                return null;
            }

            try {
                return (T) client.parseObject(obj.getContent()).asObjectable();
            } catch (Exception ex) {
                // todo fix
                ex.printStackTrace();
            }

            return null;
        }
    }
}
