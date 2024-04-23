package com.evolveum.midpoint.studio.ui.diff;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SynchronizationFile {

    private final FileItem item;

    private final List<SynchronizationObject> objects;

    public SynchronizationFile(@NotNull FileItem item, List<SynchronizationObject> objects) {
        this.item = item;
        this.objects = objects != null ? objects : List.of();
    }

    public FileItem getItem() {
        return item;
    }

    public List<SynchronizationObject> getObjects() {
        return objects;
    }
}
