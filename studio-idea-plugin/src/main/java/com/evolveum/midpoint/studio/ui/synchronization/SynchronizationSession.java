package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.action.task.ObjectsBackgroundableTask;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SynchronizationSession<T extends SynchronizationObjectItem> {

    private static final Logger LOG = Logger.getInstance(SynchronizationSession.class);

    private static final String NOTIFICATION_KEY = "Synchronization Session";

    private final Project project;

    private final Environment environment;

    private final SynchronizationPanel panel;

    private boolean closed;

    private final List<SynchronizationFileItem<T>> items = new ArrayList<>();

    public SynchronizationSession(
            @NotNull Project project, @NotNull Environment environment, @NotNull SynchronizationPanel panel) {

        this.project = project;
        this.environment = environment;

        this.panel = panel;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void open() {

    }

    public void close() {
        this.closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    public void addFileItem(@NotNull SynchronizationFileItem<T> item) {
        items.add(item);

        RunnableUtils.invokeLaterIfNeeded(() -> {
            panel.getModel().addFiles(List.of(item));
        });
    }

    public void replaceFileItem(
            @NotNull SynchronizationFileItem<T> oldItem, @Nullable SynchronizationFileItem<T> newItem) {

        if (newItem != null) {
            items.add(items.indexOf(oldItem), newItem);
        }
        items.remove(oldItem);

        // todo close related editors when removing item from session

        RunnableUtils.invokeLaterIfNeeded(() -> {
            panel.getModel().setData((List) items);
            // todo improve
//            panel.getModel().replaceFiles(List.of(newItem));
        });
    }

    public void replaceObjectItem(
            @NotNull SynchronizationObjectItem oldItem, @Nullable SynchronizationObjectItem newItem) {

        SynchronizationFileItem fileItem = oldItem.getFileItem();
        List<T> objectItems = fileItem.getObjects();
        if (newItem != null) {
            objectItems.add(objectItems.indexOf(oldItem), (T) newItem);
        }
        objectItems.remove(oldItem);

        // todo close related editors when removing item from session

        RunnableUtils.invokeLaterIfNeeded(() -> {
            panel.getModel().setData((List) items);
//            todo improve
//            panel.getModel().replaceObjects(List.of(newItem));
        });
    }

    public void updateRemote(List<SynchronizationObjectItem> objectItems) {
        if (objectItems.isEmpty()) {
            return;
        }

        List<MidPointObject> objects = objectItems.stream()
                .filter(i -> i.getRemote() != null)
                .map(item -> {
                    try {
//                        if (item.getRemote() != null) {
                        MidPointObject object = item.getRemote().copy();

                        PrismObject<?> remote = item.getRemoteObject().getCurrent();
                        String xml = ClientUtils.serialize(PrismContext.get(), remote);
                        object.setContent(xml);

                        return object;
//                        } else {
//                            // remote doesn't exist yet...
//                            PrismObject<?> local = item.getLocalObject().getCurrent();
//                            String xml = ClientUtils.serialize(PrismContext.get(), local);
//
//                            return new MidPointObject(
//                                    xml, ObjectTypes.getObjectTypeFromTypeQName(local.getDefinition().getTypeName()), false);
//                        }
                    } catch (Exception ex) {
                        LOG.debug("Couldn't serialize remote object: " + item.getName(), ex);

                        MidPointUtils.publishExceptionNotification(
                                project, environment, SynchronizationSession.class, NOTIFICATION_KEY,
                                "Couldn't serialize remote object: " + item.getRemote().getName(), ex);

                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        UploadFullProcessingTask task = new UploadFullProcessingTask(project, null, environment);
        task.setObjects(objects);
        task.setEnvironment(environment);
        task.addTaskListener(new FullUploadTaskListener(objectItems));

        ProgressManager.getInstance().run(task);
    }

    public void saveLocally(List<SynchronizationObjectItem> objectItems) {
        if (objectItems.isEmpty()) {
            return;
        }

        RunnableUtils.invokeLaterIfNeeded(() -> {
            WriteAction.run(() -> {
                writeLocally(objectItems);
            });
        });
    }

    private Map<SynchronizationFileItem<?>, List<SynchronizationObjectItem>> createFileMap(
            List<SynchronizationObjectItem> objectItems) {

        Map<SynchronizationFileItem<?>, List<SynchronizationObjectItem>> map = new HashMap<>();
        objectItems.forEach(i -> {
            List<SynchronizationObjectItem> objects = map.getOrDefault(i.getFileItem(), new ArrayList<>());
            objects.add(i);
            map.put(i.getFileItem(), objects);
        });

        return map;
    }

    private void writeLocally(List<SynchronizationObjectItem> objectItems) {
        Map<SynchronizationFileItem<?>, List<SynchronizationObjectItem>> updates = createFileMap(objectItems);

        for (SynchronizationFileItem<?> fileItem : updates.keySet()) {
            try {
                List<SynchronizationObjectItem> fileObjects = updates.get(fileItem);

                String content = prepareContent(fileItem, fileObjects);

                VirtualFile file = fileItem.getFile();
                VfsUtil.saveText(file, content);

                fileObjects.forEach(o -> o.getLocalObject().commit());
            } catch (Exception ex) {
                LOG.debug("Couldn't save local changes to file: " + fileItem.getFile().getName(), ex);

                MidPointUtils.publishExceptionNotification(
                        project, environment, SynchronizationSession.class, NOTIFICATION_KEY,
                        "Couldn't save local changes to file: " + fileItem.getFile().getName(), ex);
            }
        }
    }

    private String prepareContent(SynchronizationFileItem<?> fileItem, List<SynchronizationObjectItem> toUpdate)
            throws SchemaException {

        List<PrismObject<?>> objects = new ArrayList<>();
        for (SynchronizationObjectItem soi : fileItem.getObjects()) {
            PrismObjectHolder<?> localObjectStateful = soi.getLocalObject();

            PrismObject<?> localObject = toUpdate.contains(soi) ? localObjectStateful.getCurrent() : localObjectStateful.getOriginal();

            objects.add(localObject);
        }

        return serializeObjects(objects);
    }

    private String serializeObjects(List<PrismObject<?>> objects) throws SchemaException {
        if (objects.size() == 1) {
            return PrismContext.get().xmlSerializer().serialize(objects.get(0));
        }

        return PrismContext.get().xmlSerializer().serializeObjects(objects);
    }

    private static class FullUploadTaskListener implements ObjectsBackgroundableTask.TaskListener {

        private List<SynchronizationObjectItem> objectItems;

        public FullUploadTaskListener(List<SynchronizationObjectItem> objectItems) {
            this.objectItems = objectItems;
        }

        @Override
        public void objectProcessed(MidPointObject object, ProcessObjectResult result) {
            SynchronizationObjectItem objectItem = objectItems.stream()
                    .filter(i -> Objects.equals(i.getOid(), object.getOid()))
                    .findFirst()
                    .orElse(null);

            if (objectItem == null) {
                return;
            }

            if (!result.problem()) {
                objectItem.getRemoteObject().commit();
            }
        }
    }
}
