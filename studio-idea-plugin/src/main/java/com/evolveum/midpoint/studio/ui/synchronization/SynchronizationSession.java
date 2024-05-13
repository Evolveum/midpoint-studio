package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.ui.diff.SynchronizationPanel;
import com.evolveum.midpoint.studio.util.RunnableUtils;
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
        RunnableUtils.invokeLaterIfNeeded(() -> {
            panel.getModel().setData(new ArrayList<>());
        });
    }

    public void close() {
        this.closed = true;
        // todo notify ui/tree
    }

    public boolean isClosed() {
        return closed;
    }

    public void addItem(@NotNull SynchronizationFileItem<T> item) {
        items.add(item);

        // todo notify ui/tree

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

        // todo execute in write thread

        Map<SynchronizationFileItem, List<SynchronizationObjectItem>> updates = new HashMap<>();
        objectItems.forEach(i -> {
            List<SynchronizationObjectItem> objects = updates.getOrDefault(i.getFileItem(), new ArrayList<>());
            objects.add(i);
            updates.put(i.getFileItem(), objects);
        });

        // todo something

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

    private String prepareContent(SynchronizationFileItem fileItem, List<SynchronizationObjectItem> toUpdate) {
        List<SynchronizationObjectItem> objectItems = fileItem.getObjects();

        List<PrismObject<?>> objects = new ArrayList<>();

        // todo implement

        return null;
    }

    private String serializeObjects() {
        // todo implement

        return null;
    }
}
