package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.common.cleanup.CleanupActionProcessor;
import com.evolveum.midpoint.common.cleanup.CleanupEvent;
import com.evolveum.midpoint.common.cleanup.CleanupListener;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.PrismQuerySerialization;
import com.evolveum.midpoint.prism.query.builder.S_MatchingRuleEntry;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileTask extends ClientBackgroundableTask<TaskState> {

    public static final String TITLE = "Cleanup File";

    public static final String NOTIFICATION_KEY = "Cleanup File Action";

    private static final Logger LOG = Logger.getInstance(CleanupFileTask.class);

    private static final ModuleDescriptor.Version CONNECTOR_AVAILABLE_SUPPORT_VERSION =
            ModuleDescriptor.Version.parse("4.6");

    private static final ModuleDescriptor.Version REFERENCE_FILTER_SUPPORT_VERSION =
            ModuleDescriptor.Version.parse("4.4");

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
                    CleanupFileTask.this.onReferenceCleanup(event, object);
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
            LOG.error("Couldn't cleanup content for object " + object.getName(), ex);
        }

        return content;
    }

    private boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
        int result = MidPointUtils.showConfirmationDialog(
                getProject(), null, "Do you really want to remove item " + event.getPath() + "?",
                "Confirm remove", "Remove", "Skip");

        return result == MessageDialog.OK_EXIT_CODE;
    }

    private void onReferenceCleanup(CleanupEvent<PrismReference> event, MidPointObject objectSource) {
        PrismObject<?> object = event.getObject();
        if (ResourceType.class.equals(object.getCompileTimeClass())
                && ResourceType.F_CONNECTOR_REF.equivalent(event.getPath())) {
            processConnectorRef(event, objectSource);
            return;
        }

        PrismReference ref = event.getValue();
        if (ref.isEmpty()) {
            return;
        }

        ref.getValues().forEach(refValue -> processOtherRef(event, refValue));
    }

    private void processOtherRef(CleanupEvent<PrismReference> event, PrismReferenceValue refValue) {
        String oid = refValue.getOid();
        List<VirtualFile> files = ObjectFileBasedIndexImpl.getVirtualFiles(oid, getProject(), true);
        if (!files.isEmpty()) {
            return;
        }

        CleanupService cs = CleanupService.get(getProject());
        if (!cs.getSettings().isWarnAboutMissingReferences()) {
            return;
        }

        QName type = refValue.getTargetType();
        if (type == null) {
            type = ObjectType.COMPLEX_TYPE;
        }

        PrismObject<?> object = event.getObject();

        StringBuilder sb = new StringBuilder();
        sb
                .append("Object '")
                .append(MidPointUtils.getName(object))
                .append("' ('")
                .append(object.getOid())
                .append("') contains reference ")
                .append(refValue.getOid())
                .append(" (")
                .append(type.getLocalPart())
                .append(") that couldn't be resolved from downloaded files. Make sure this is not a problem.");

        MidPointUtils.publishNotification(getProject(), notificationKey, "Unresolved reference", sb.toString(), NotificationType.WARNING);

        //todo implement, validate reference (whether it's available locally)
    }

    private void processConnectorRef(CleanupEvent<PrismReference> event, MidPointObject objectSource) {
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
                MidPointUtils.publishNotification(
                        getProject(), notificationKey, "Unresolved connector reference",
                        "Couldn't find connector with oid " + oid + " in selected environment", NotificationType.WARNING);
                return;
            }

            String xml = object.getContent();
            PrismObject<ConnectorType> connector = (PrismObject<ConnectorType>) client.parseObject(xml);
            ConnectorType connectorType = connector.asObjectable();

            SearchFilterType searchFilter = createSearchFilterType(objectSource.getContent(), connectorType);
            if (searchFilter != null) {
                val.setFilter(searchFilter);
                clearOidFromReference(val);
            }
        } catch (Exception ex) {
            LOG.error("Couldn't get connector with oid " + oid, ex);
        }
    }

    private SearchFilterType createSearchFilterType(String resourceXml, ConnectorType connectorType) throws PrismQuerySerialization.NotSupportedException, SchemaException {
        PrismContext prismContext = client.getPrismContext();

        S_MatchingRuleEntry filterBuilder = prismContext.queryFor(ConnectorType.class)
                .item(ConnectorType.F_CONNECTOR_TYPE).eq(connectorType.getConnectorType())
                .and()
                .item(ConnectorType.F_CONNECTOR_VERSION).eq(connectorType.getConnectorVersion());

        if (shouldAddConnectorAvailable()) {
            filterBuilder = filterBuilder
                    .and()
                    .item(ConnectorType.F_AVAILABLE).eq(true);
        }

        ObjectFilter filter = filterBuilder.buildFilter();
        if (!shouldUseFilterText()) {
            return prismContext.getQueryConverter().createSearchFilterType(filter);
        }

        PrismNamespaceContext nsCtx = getPrismNamespaceContextForConnectorRef(resourceXml);
        String filterText = prismContext.querySerializer().serialize(filter, nsCtx).filterText();
        SearchFilterType searchFilter = new SearchFilterType();
        searchFilter.setText(filterText);

        return searchFilter;
    }

    private PrismNamespaceContext getPrismNamespaceContextForConnectorRef(String resourceXml) throws SchemaException {
        PrismContext ctx = client.getPrismContext();
        XNode node = ctx.parserFor(resourceXml).parseToXNode().getSubnode();
        if (!(node instanceof MapXNode mapNode)) {
            return node.namespaceContext();
        }

        return mapNode.get(ResourceType.F_CONNECTOR_REF).namespaceContext();
    }

    private String getMidpointVersion() {
        MidPointService ms = MidPointService.get(getProject());
        String current = ms.getSettings().getMidpointVersion();

        return current != null ? current : MidPointConstants.DEFAULT_MIDPOINT_VERSION;
    }

    private boolean shouldAddConnectorAvailable() {
        String current = getMidpointVersion();
        return current == null ||
                CONNECTOR_AVAILABLE_SUPPORT_VERSION.compareTo(ModuleDescriptor.Version.parse(current)) <= 0;
    }

    private boolean shouldUseFilterText() {
        String current = getMidpointVersion();
        return current == null ||
                REFERENCE_FILTER_SUPPORT_VERSION.compareTo(ModuleDescriptor.Version.parse(current)) <= 0;
    }

    private void clearOidFromReference(PrismReferenceValue value) {
        value.setOid(null);
        value.setRelation(null);
        value.setTargetType(null);
    }
}
