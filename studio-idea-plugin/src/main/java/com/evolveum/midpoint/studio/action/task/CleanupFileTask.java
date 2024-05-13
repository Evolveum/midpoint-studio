package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.common.cleanup.CleanupItem;
import com.evolveum.midpoint.common.cleanup.CleanupItemType;
import com.evolveum.midpoint.common.cleanup.CleanupResult;
import com.evolveum.midpoint.common.cleanup.ObjectCleaner;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.validator.ObjectValidator;
import com.evolveum.midpoint.schema.validator.ValidationItem;
import com.evolveum.midpoint.schema.validator.ValidationResult;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefKey;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefUtils;
import com.evolveum.midpoint.studio.util.MavenUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
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

    public CleanupFileTask(@NotNull Project project, Supplier<DataContext> dataContextSupplier, Environment environment) {
        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);

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

            ObjectCleaner processor = cs.createCleanupProcessor();
            processor.setListener(new StudioCleanupListener(getProject(), client, MidPointUtils.DEFAULT_PRISM_CONTEXT));

            ObjectValidator validator = new ObjectValidator();
            validator.setAllWarnings();

            String current = MavenUtils.getMidpointVersion(getProject());
            if (current == null) {
                current = MidPointConstants.DEFAULT_MIDPOINT_VERSION;
            }
            validator.setWarnPlannedRemovalVersion(current);

            for (PrismObject<? extends ObjectType> obj : clonedObjects) {
                if (ResourceType.COMPLEX_TYPE.equals(obj.getDefinition().getTypeName())) {
                    applyConnectorSchema((PrismObject<ResourceType>) obj);
                }

                CleanupResult cleanupResult = processor.process(obj);

                ValidationResult validationResult = validator.validate(obj);

                updateMissingReferencesSummary(object, cleanupResult.getMessages());

                publishNotification(object, cleanupResult.getMessages(), validationResult.getItems());
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

    private void applyConnectorSchema(PrismObject<ResourceType> resource) {
        // todo apply connector schema
    }

    private void updateMissingReferencesSummary(MidPointObject object, List<CleanupItem<?>> messages) {
        MissingRefObject missingRefObject =
                buildObjectReferencesConfiguration(
                        object.getOid(), object.getType(), messages);
        if (!missingRefObject.isEmpty()) {
            missingReferencesSummary.add(missingRefObject);
        }
    }

    private void publishNotification(
            MidPointObject object, List<CleanupItem<?>> cleanupMessages, List<ValidationItem> validationMessages) {

        List<CleanupItem<?>> cleanupFiltered = cleanupMessages.stream()
                .filter(m -> {
                    if (m.type() != CleanupItemType.MISSING_REFERENCE) {
                        return true;
                    }

                    ObjectReferenceType ref = (ObjectReferenceType) m.data();

                    MissingRefAction action = configActionMap
                            .getOrDefault(new MissingRefKey(object.getOid(), object.getType().getTypeQName()), Map.of())
                            .get(new MissingRefKey(ref.getOid(), ref.getType()));
                    if (action == null) {
                        action = defaultAction;
                    }

                    return action == MissingRefAction.DOWNLOAD;
                })
                .toList();

        Map<String, Long> cleanupCounted = cleanupFiltered.stream()
                .collect(Collectors.groupingBy(cm -> cm.message().getFallbackMessage(), Collectors.counting()));

        Map<String, Long> validationCounted = validationMessages.stream()
                .collect(Collectors.groupingBy(v -> v.message().getFallbackMessage(), Collectors.counting()));

        Map<String, Long> all = new HashMap<>();
        all.putAll(cleanupCounted);
        all.putAll(validationCounted);

        List<String> msgs = all.keySet().stream()
                .map(m -> {
                    String msg = m;

                    if (cleanupCounted.getOrDefault(m, 0L) > 1) {
                        msg += " (" + cleanupCounted.get(m) + ")";
                    }

                    return msg;
                })
                .sorted()
                .collect(Collectors.toList());

        if (msgs.isEmpty()) {
            return;
        }

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
                                buildObjectReferencesConfiguration(object.getOid(), object.getType(), cleanupMessages)
                        )
                )
        );
    }

    private MissingRefObject buildObjectReferencesConfiguration(
            String oid, ObjectTypes type, List<CleanupItem<?>> cleanupMessages) {

        List<ObjectReferenceType> missingReferences = cleanupMessages.stream()
                .filter(m -> m.type() == CleanupItemType.MISSING_REFERENCE)
                .map(m -> (ObjectReferenceType) m.data())
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

    @Override
    protected NotificationAction[] getNotificationActionsAfterFinish() {
        // todo fix this for whole cleanup - how to compute missing references and download only
        //  ...and oid/type if this is for all

        return createNotificationActions(null, new ArrayList<>(missingReferencesSummary));
    }
}
