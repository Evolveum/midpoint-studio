package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.ui.diff.SynchronizationPanel;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynchronizationSession<T extends SynchronizationObjectItem> {

    private final Environment environment;

    private final SynchronizationPanel panel;

    private boolean closed;

    private final List<SynchronizationFileItem<T>> items = new ArrayList<>();

    public SynchronizationSession(@NotNull Environment environment, @NotNull SynchronizationPanel panel) {
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

        for (SynchronizationFileItem fileItem : updates.keySet()) {
            try {
                String content = prepareContent(fileItem, updates.get(fileItem));

                VirtualFile file = fileItem.getFile();
                VfsUtil.saveText(file, content);
            } catch (Exception ex) {
                // todo handle
                ex.printStackTrace();
            }
        }
    }

    private String prepareContent(SynchronizationFileItem<?> fileItem, List<SynchronizationObjectItem> toUpdate)
            throws SchemaException {

        List<PrismObject<?>> objects = new ArrayList<>();
        for (SynchronizationObjectItem soi : fileItem.getObjects()) {
            objects.add(soi.getLocalObject().getCurrent());
        }

        return serializeObjects(objects);
    }

    private String serializeObjects(List<PrismObject<?>> objects) throws SchemaException {
        return PrismContext.get().xmlSerializer().serializeObjects(objects);
    }
}
