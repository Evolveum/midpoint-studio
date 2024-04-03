package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.common.cleanup.*;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.configuration.*;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefKey;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefUtils;
import com.evolveum.midpoint.studio.util.MavenUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileTask extends ClientBackgroundableTask<TaskState> {

    public static final String TITLE = "Cleanup File";

    public static final String NOTIFICATION_KEY = "Cleanup File Action";

    private static final Logger LOG = Logger.getInstance(CleanupFileTask.class);

    /**
     * Summary of missing references for all objects (files) processed using this task.
     * <p>
     * Used for notification actions.
     */
    private List<MissingRefObject> missingReferencesSummary;

    private Map<MissingRefKey, Map<MissingRefKey, MissingRefAction>> configActionMap;

    private MissingRefAction defaultAction;

    public CleanupFileTask(@NotNull AnActionEvent event, Environment environment) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
        setEnvironment(environment);
    }

    @Override
    protected MidPointClient setupMidpointClient() {
        Environment env = getEnvironment();

        return new MidPointClient(getProject(), env, true, true);
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        missingReferencesSummary = new ArrayList<>();

        CleanupService cs = CleanupService.get(getProject());

        configActionMap = MissingRefUtils.buildActionMapFromConfiguration(getProject());

        defaultAction = cs.getSettings().getMissingReferences().getDefaultAction();
        if (defaultAction == null) {
            defaultAction = MissingRefAction.UNDEFINED;
        }

        super.doRun(indicator);

        missingReferencesSummary.clear();
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

            List<PrismObject<? extends ObjectType>> clonedObjects = (List) objects.stream()
                    .map(PrismObject::clone)
                    .toList();

            CleanupService cs = CleanupService.get(getProject());

            CleanupActionProcessor processor = cs.createCleanupProcessor();

            DefaultCleanupHandler handler = getHandler(cs);
            processor.setHandler(handler);

            for (PrismObject<? extends ObjectType> obj : clonedObjects) {
                CleanupResult result = processor.process(obj, Source.of(object.getFile(), object.getContent()));

                updateMissingReferencesSummary(object, result.getMessages());

                publishNotification(object, result.getMessages());
            }

            if (objects.equals(clonedObjects)) {
                return content;
            }

            PrismSerializer<String> serializer = ClientUtils.getSerializer(MidPointUtils.DEFAULT_PRISM_CONTEXT);
            if (clonedObjects.size() == 1) {
                return serializer.serialize(clonedObjects.get(0));
            }

            return serializer.serializeObjects((List) clonedObjects);
        } catch (SchemaException | IOException ex) {
            LOG.error("Couldn't cleanup content for object " + object.getName(), ex);
        }

        return content;
    }

    private void updateMissingReferencesSummary(MidPointObject object, List<CleanupMessage<?>> messages) {
        MissingRefObject missingRefObject =
                buildObjectReferencesConfiguration(
                        object.getOid(), object.getType(), messages);
        if (!missingRefObject.isEmpty()) {
            missingReferencesSummary.add(missingRefObject);
        }
    }

    @NotNull
    private DefaultCleanupHandler getHandler(CleanupService cs) {
        DefaultCleanupHandler handler = new DefaultCleanupHandler(MidPointUtils.DEFAULT_PRISM_CONTEXT) {

            @Override
            public boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
                return CleanupFileTask.this.onConfirmOptionalCleanup(event);
            }

            @Override
            protected String getMidpointVersion() {
                String current = MavenUtils.getMidpointVersion(getProject());
                return current != null ? current : MidPointConstants.DEFAULT_MIDPOINT_VERSION;
            }

            @Override
            protected <O extends ObjectType> boolean canResolveLocalObject(Class<O> type, String oid) {
                return CleanupFileTask.this.canResolveLocalObject(type, oid);
            }

            @Override
            protected PrismObject<ConnectorType> resolveConnector(String oid) {
                return CleanupFileTask.this.resolveConnector(oid);
            }
        };

        CleanupConfiguration configuration = cs.getSettings();
        handler.setWarnAboutMissingReferences(configuration.isWarnAboutMissingReferences());

        return handler;
    }

    private <O extends ObjectType> PrismObject<O> resolveConnector(String oid) {
        try {
            MidPointObject object = client.get(ConnectorType.class, oid, new SearchOptions().raw(true));
            if (object == null) {
                return null;
            }

            return (PrismObject<O>) client.parseObject(object.getContent());
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    private <O extends ObjectType> boolean canResolveLocalObject(Class<O> type, String oid) {
        if (oid == null) {
            return false;
        }

        List<VirtualFile> files = ApplicationManager.getApplication().runReadAction(
                (Computable<List<VirtualFile>>) () ->
                        ObjectFileBasedIndexImpl.getVirtualFiles(oid, getProject(), true));

        return !files.isEmpty();
    }

    private void publishNotification(MidPointObject object, List<CleanupMessage<?>> messages) {
        if (messages.isEmpty()) {
            return;
        }

        Map<String, Long> messagesCounted = messages.stream()
                .collect(Collectors.groupingBy(cm -> cm.message().getFallbackMessage(), Collectors.counting()));

        List<String> msgs = messagesCounted.keySet().stream()
                .map(m -> {
                    String msg = m;

                    if (messagesCounted.getOrDefault(m, 0L) > 1) {
                        msg += " (" + messagesCounted.get(m) + ")";
                    }

                    return msg;
                })
                .sorted()
                .collect(Collectors.toList());

        String msg = "Cleanup warnings for object '" + object.getName() + "':<br/><br/>"
                + StringUtils.join(msgs, "<br/>");

        VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(object.getFile().toPath());

        MidPointUtils.publishNotification(
                getProject(),
                notificationKey,
                "Cleanup warning",
                msg,
                NotificationType.WARNING,
                createNotificationActions(
                        file,
                        List.of(
                                buildObjectReferencesConfiguration(object.getOid(), object.getType(), messages)
                        )
                )
        );
    }

    private MissingRefObject buildObjectReferencesConfiguration(
            String oid, ObjectTypes type, List<CleanupMessage<?>> cleanupMessages) {

        List<ObjectReferenceType> missingReferences = cleanupMessages.stream()
                .filter(m -> m.type() == CleanupMessage.Type.MISSING_REFERENCE)
                .map(m -> (ObjectReferenceType) m.data())
                .filter(ref -> {
                    MissingRefAction action = configActionMap.getOrDefault(new MissingRefKey(oid, type.getTypeQName()), Map.of())
                            .getOrDefault(new MissingRefKey(ref.getOid(), ref.getType()), defaultAction);

                    return action == MissingRefAction.DOWNLOAD;
                })
                .toList();

        MissingRefObject config = new MissingRefObject();
        config.setOid(oid);
        config.setType(type.getTypeQName());

        for (ObjectReferenceType ref : missingReferences) {
            MissingRef rc = new MissingRef();
            rc.setOid(ref.getOid());
            rc.setType(ref.getType());

            config.getReferences().add(rc);
        }

        return config;
    }

    private NotificationAction[] createNotificationActions(VirtualFile file, List<MissingRefObject> missingReferences) {
        List<NotificationAction> actions = new ArrayList<>();
        if (file != null) {
            actions.add(new SeeObjectNotificationAction(file));
        }

        if (!missingReferences.isEmpty()) {
            actions.add(new MissingReferencesNotificationAction(missingReferences));
        }

        DownloadMissingNotificationAction downloadAction =
                new DownloadMissingNotificationAction(getProject(), missingReferences);
        if (downloadAction.isVisible()) {
            actions.add(downloadAction);
        }

        return actions.toArray(NotificationAction[]::new);
    }

    private boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
        int result = MidPointUtils.showConfirmationDialog(
                getProject(), null, "Do you really want to remove item " + event.path() + "?",
                "Confirm remove", "Remove", "Skip");

        return result == MessageDialog.OK_EXIT_CODE;
    }

    @Override
    protected NotificationAction[] getNotificationActionsAfterFinish() {
        // todo fix this for whole cleanup - how to compute missing references and download only
        //  ...and oid/type if this is for all

        return createNotificationActions(null, new ArrayList<>(missingReferencesSummary));
    }
}
