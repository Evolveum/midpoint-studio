package com.evolveum.midpoint.studio.ui.synchronization;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SynchronizationFileItem<T extends SynchronizationObjectItem> extends SynchronizationItem {

    private final VirtualFile file;

    private final List<T> objects;

    public SynchronizationFileItem(@NotNull VirtualFile file) {
        this(file, new ArrayList<>());
    }

    public SynchronizationFileItem(@NotNull VirtualFile file, @NotNull List<T> objects) {
        super(SynchronizationItemType.FILE);

        this.file = file;
        this.objects = objects;
    }

    @Override
    public String getName() {
        if (objects.size() != 1) {
            return file.getName();
        }

        return file.getName() + " (" + objects.get(0).getName() + ")";
    }

    @NotNull
    public VirtualFile getFile() {
        return file;
    }

    @NotNull
    public List<T> getObjects() {
        return objects;
    }

    @Override
    public boolean hasLocalChanges() {
        return objects.stream().anyMatch(SynchronizationObjectItem::hasLocalChanges);
    }

    @Override
    public boolean hasRemoteChanges() {
        return objects.stream().anyMatch(SynchronizationObjectItem::hasRemoteChanges);
    }

    @Override
    public boolean isNew() {
        return objects.stream().anyMatch(SynchronizationObjectItem::isNew);
    }

    @Override
    public boolean isVisible() {
        return objects.stream().anyMatch(SynchronizationObjectItem::isVisible);
    }

    @Override
    public boolean isUnchanged() {
        return objects.stream().allMatch(SynchronizationObjectItem::isUnchanged);
    }
}
