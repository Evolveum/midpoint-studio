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
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
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
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

            CleanupService cs = CleanupService.get(getProject());
            handler.setWarnAboutMissingReferences(cs.getSettings().isWarnAboutMissingReferences());

            processor.setHandler(handler);

            for (PrismObject<? extends ObjectType> obj : result) {
                CleanupResult cleanupResult = processor.process(obj, Source.of(object.getFile(), object.getContent()));

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

    private void publishCleanupNotifications(MidPointObject object, CleanupResult result) {
        for (CleanupMessage.Status status : CleanupMessage.Status.values()) {
            List<CleanupMessage> messages = result.getMessages(status);

            publishNotification(object, status, messages, result.getMissingReferences());
        }
    }

    private void publishNotification(
            MidPointObject object, CleanupMessage.Status status, List<CleanupMessage> messages,
            List<ObjectReferenceType> missingReferences) {

        if (messages.isEmpty()) {
            return;
        }

        NotificationType type = switch (status) {
            case ERROR -> NotificationType.ERROR;
            case WARNING -> NotificationType.WARNING;
            case INFO -> NotificationType.INFORMATION;
        };

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

        String msg = "Cleanup warnings for object '" + object.getName() + "':<br/><br/>" + StringUtils.join(msgs, "<br/>");

        VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(object.getFile().toPath());

        MidPointUtils.publishNotification(
                getProject(), notificationKey, "Cleanup warning", msg, type,
                new SeeObjectNotificationAction(file), new DownloadMissingNotificationAction(missingReferences));
    }

    private boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
        int result = MidPointUtils.showConfirmationDialog(
                getProject(), null, "Do you really want to remove item " + event.path() + "?",
                "Confirm remove", "Remove", "Skip");

        return result == MessageDialog.OK_EXIT_CODE;
    }

    @Override
    protected NotificationAction[] getNotificationActionsAfterFinish() {
        return new NotificationAction[]{
                new DownloadMissingNotificationAction(List.of())    // todo fix
        };
    }
}
