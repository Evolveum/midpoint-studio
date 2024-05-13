package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.ui.diff.SynchronizationPanel;
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

    public void open() {

    }

    public void close() {
        this.closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    public void addItem(@NotNull SynchronizationFileItem<T> item) {
        items.add(item);

        RunnableUtils.invokeLaterIfNeeded(() -> {
            panel.getModel().addFiles(List.of(item));
        });
    }

    public void updateRemote(List<SynchronizationObjectItem> objectItems) {
        if (objectItems.isEmpty()) {
            return;
        }

        List<MidPointObject> objects = objectItems.stream()
                .map(item -> {
                    try {
                        MidPointObject object = item.getRemote().copy();

                        PrismObject<?> remote = item.getRemoteObject().getCurrent();
                        String xml = ClientUtils.serialize(PrismContext.get(), remote);
                        object.setContent(xml);

                        return object;
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
        // todo add listener for processed objects to commit changes into item (remote holder)
        task.setObjects(objects);
        task.setEnvironment(environment);

        ProgressManager.getInstance().run(task);

        // todo implement
    }

    public void refresh(List<SynchronizationObjectItem> objectItems) {
        if (objectItems.isEmpty()) {
            return;
        }

        // todo implement
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
        return PrismContext.get().xmlSerializer().serializeObjects(objects);
    }
}
