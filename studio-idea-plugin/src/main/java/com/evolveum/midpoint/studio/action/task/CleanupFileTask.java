package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.common.cleanup.*;
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
import com.evolveum.midpoint.studio.impl.DownloadMissingNotificationAction;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.impl.SeeObjectNotificationAction;
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.util.MavenUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.SingleLocalizableMessage;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

                @Override
                public void onProtectedStringCleanup(CleanupEvent<PrismProperty<ProtectedStringType>> event) {
                    CleanupFileTask.this.onProtectedStringCleanup(event);
                }
            });

            for (PrismObject<? extends ObjectType> obj : result) {
                CleanupResult cleanupResult = processor.process(obj);

                publishCleanupNotifications(object, cleanupResult);
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

    private void publishCleanupNotifications(MidPointObject object, CleanupResult result) {
        for (CleanupMessage.Status status : CleanupMessage.Status.values()) {
            publishNotification(object, status, result.getMessages(status));
        }
    }

    private void publishNotification(MidPointObject object, CleanupMessage.Status status, List<CleanupMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }

        NotificationType type = switch (status) {
            case ERROR -> NotificationType.ERROR;
            case WARNING -> NotificationType.WARNING;
            case INFO -> NotificationType.INFORMATION;
        };

        List<String> msgs = messages.stream().map(cm -> cm.message().getFallbackMessage()).collect(Collectors.toList());
        String msg = "Cleanup warnings for object '" + object.getName() + "':<br/><br/>" + StringUtils.join(msgs, "<br/>");

        VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(object.getFile().toPath());

        MidPointUtils.publishNotification(
                getProject(), notificationKey, "Cleanup warning", msg, type,
                new SeeObjectNotificationAction(file), new DownloadMissingNotificationAction());
    }

    private void onProtectedStringCleanup(CleanupEvent<PrismProperty<ProtectedStringType>> event) {
        PrismProperty<ProtectedStringType> property = event.value();
        if (property.isEmpty()) {
            return;
        }

        List<String> messages = new ArrayList<>();
        for (PrismPropertyValue<ProtectedStringType> value : property.getValues()) {
            ProtectedStringType ps = value.getValue();
            if (ps == null) {
                continue;
            }

            if (ps.getEncryptedDataType() != null) {
                messages.add("encrypted data in " + property.getPath());
            }

            if (ps.getHashedDataType() != null) {
                messages.add("hashed data in " + property.getPath());
            }

            if (ps.getClearValue() != null) {
                messages.add("clear value in " + property.getPath());
            }
        }

        if (messages.isEmpty()) {
            return;
        }

        event.result().getMessages().add(
                new CleanupMessage(
                        CleanupMessage.Status.WARNING,
                        new SingleLocalizableMessage(
                                "Protected string: " + StringUtils.join(messages, ", "))));
    }

    private boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
        int result = MidPointUtils.showConfirmationDialog(
                getProject(), null, "Do you really want to remove item " + event.path() + "?",
                "Confirm remove", "Remove", "Skip");

        return result == MessageDialog.OK_EXIT_CODE;
    }

    private void onReferenceCleanup(CleanupEvent<PrismReference> event, MidPointObject objectSource) {
        PrismObject<?> object = event.object();
        if (ResourceType.class.equals(object.getCompileTimeClass())
                && ResourceType.F_CONNECTOR_REF.equivalent(event.path())) {
            processConnectorRef(event, objectSource);
            return;
        }

        PrismReference ref = event.value();
        if (ref.isEmpty()) {
            return;
        }

        ref.getValues().forEach(refValue -> processOtherRef(event, refValue));
    }

    private void processOtherRef(CleanupEvent<PrismReference> event, PrismReferenceValue refValue) {
        String oid = refValue.getOid();

        List<VirtualFile> files = ApplicationManager.getApplication().runReadAction(
                (Computable<List<VirtualFile>>) () ->
                        ObjectFileBasedIndexImpl.getVirtualFiles(oid, getProject(), true));

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

        event.result().getMessages().add(
                new CleanupMessage(
                        CleanupMessage.Status.WARNING,
                        new SingleLocalizableMessage(
                                "Unresolved reference (in project): " + refValue.getOid() + "(" + type.getLocalPart() + ").")));

        //todo implement, validate reference (whether it's available locally)
    }

    private void processConnectorRef(CleanupEvent<PrismReference> event, MidPointObject objectSource) {
        PrismReference ref = event.value();
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
                event.result().getMessages().add(
                        new CleanupMessage(
                                CleanupMessage.Status.WARNING,
                                new SingleLocalizableMessage(
                                        "Unresolved connector reference: Couldn't find connector with oid "
                                                + oid + " in selected environment.")));
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
        String current = MavenUtils.getMidpointVersion(getProject());
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
