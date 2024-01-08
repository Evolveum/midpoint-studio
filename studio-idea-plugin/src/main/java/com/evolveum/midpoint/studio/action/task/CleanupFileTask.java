package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.common.cleanup.CleanupActionProcessor;
import com.evolveum.midpoint.common.cleanup.CleanupEvent;
import com.evolveum.midpoint.common.cleanup.CleanupListener;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.messages.MessageDialog;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileTask extends ClientBackgroundableTask<TaskState> {

    public static final String TITLE = "Cleanup File";

    public static final String NOTIFICATION_KEY = "Cleanup File Action";

    private static final Logger LOG = Logger.getInstance(CleanupFileTask.class);

    public CleanupFileTask(@NotNull AnActionEvent event, Environment environment) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
        setEnvironment(environment);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject object) {
        String oldContent = object.getContent();
        String newContent = cleanupObject(object);

        if (Objects.equals(oldContent, newContent)) {
            return new ProcessObjectResult(null)
                    .object(object);
        }

        MidPointObject newObject = MidPointObject.copy(object);
        newObject.setContent(newContent);

        return new ProcessObjectResult(null)
                .object(newObject);
    }

    @Override
    protected boolean isUpdateObjectAfterProcessing() {
        return true;
    }

    @Override
    public ProcessObjectResult processObjectOid(ObjectTypes type, String oid) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private String cleanupObject(MidPointObject object) {
        String content = object.getContent();
        if (content == null) {
            return null;
        }

        try {
            PrismParser parser = ClientUtils.createParser(MidPointUtils.DEFAULT_PRISM_CONTEXT, content);
            List<PrismObject<? extends ObjectType>> objects = (List) parser.parseObjects();
            if (objects.isEmpty()) {
                return content;
            }

            List<PrismObject<? extends ObjectType>> result = (List) objects.stream().map(PrismObject::clone).toList();

            CleanupService cleanupService = CleanupService.get(getProject());

            CleanupActionProcessor processor = cleanupService.createCleanupProcessor();
            processor.setListener(new CleanupListener() {

                @Override
                public boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
                    return CleanupFileTask.this.onConfirmOptionalCleanup(event);
                }

                @Override
                public void onReferenceCleanup(CleanupEvent<PrismReference> event) {
                    CleanupFileTask.this.onReferenceCleanup(event);
                }
            });

            for (PrismObject<? extends ObjectType> obj : result) {
                processor.process(obj);
            }

            if (objects.equals(result)) {
                return content;
            }

            PrismSerializer<String> serializer = ClientUtils.getSerializer(MidPointUtils.DEFAULT_PRISM_CONTEXT);
            if (result.size() == 1) {
                return serializer.serialize(result.get(0));
            }

            return serializer.serializeObjects((List) result);
        } catch (SchemaException | IOException ex) {
            // todo handle properly
            ex.printStackTrace();
        }

        return content;
    }

    private boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
        int result = MidPointUtils.showConfirmationDialog(
                getProject(), null, "Do you really want to remove item " + event.getPath() + "?",
                "Confirm remove", "Remove", "Skip");

        return result == MessageDialog.OK_EXIT_CODE;
    }

    private void onReferenceCleanup(CleanupEvent<PrismReference> event) {
        PrismObject<?> object = event.getObject();
        if (ResourceType.class.equals(object.getCompileTimeClass()) && ResourceType.F_CONNECTOR_REF.equivalent(event.getPath())) {
            processConnectorRef(event);
            return;
        }

        processOtherRef(event);
    }

    private void processOtherRef(CleanupEvent<PrismReference> event) {
        // todo implement, validate reference (whether it's available locally)
    }

    private void processConnectorRef(CleanupEvent<PrismReference> event) {
        PrismReference ref = event.getValue();
        if (ref.isEmpty()) {
            return;
        }

        PrismReferenceValue val = ref.getValue();
        String oid = val.getOid();
        if (StringUtils.isEmpty(oid)) {
            return;
        }

        if (val.getFilter() != null) {
            clearOidFromReference(val);
            return;
        }

        try {
            MidPointObject object = client.get(ConnectorType.class, oid, new SearchOptions().raw(true));
            if (object == null) {
                // todo warning
                return;
            }

            String xml = object.getContent();
            PrismObject<ConnectorType> connector = (PrismObject<ConnectorType>) client.parseObject(xml);
            ConnectorType connectorType = connector.asObjectable();

            PrismContext prismContext = client.getPrismContext();

            // todo xml filter should be used for MP < 4.4, available in item should be used for MP >= 4.6

            ObjectFilter filter = prismContext.queryFor(ConnectorType.class)
                    .item(ConnectorType.F_CONNECTOR_TYPE).eq(connectorType.getConnectorType())
                    .and()
                    .item(ConnectorType.F_CONNECTOR_VERSION).eq(connectorType.getConnectorVersion())
                    .and()
                    .item(ConnectorType.F_AVAILABLE).eq(true)
                    .buildFilter();

//            SearchFilterType searchFilter = prismContext.getQueryConverter().createSearchFilterType(filter);
//            val.setFilter(searchFilter);

//            prismContext.parserFor(xml).parseToXNode().namespaceContext();

            // todo this created filter with wrong namespaces...

            PrismNamespaceContext.Builder builder = PrismNamespaceContext.EMPTY.childBuilder();
            builder.defaultNamespace(SchemaConstantsGenerated.NS_COMMON);
            builder.addPrefix("", SchemaConstantsGenerated.NS_COMMON);

            String filterText = prismContext.querySerializer().serialize(filter, builder.build()).filterText();
            SearchFilterType searchFilter = new SearchFilterType();
            searchFilter.setText(filterText);
            val.setFilter(searchFilter);

            clearOidFromReference(val);
        } catch (Exception ex) {
            // todo handle
            ex.printStackTrace();
        }
    }

    private void clearOidFromReference(PrismReferenceValue value) {
        value.setOid(null);
        value.setRelation(null);
        value.setTargetType(null);
    }
}
