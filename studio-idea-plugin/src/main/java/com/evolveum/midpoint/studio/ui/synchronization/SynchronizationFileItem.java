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
        return file.getName();
    }

    @NotNull
    public VirtualFile getFile() {
        return file;
    }

    @NotNull
    public List<T> getObjects() {
        return objects;
    }
}
