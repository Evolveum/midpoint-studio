package com.evolveum.midpoint.studio.ui.diff;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SyncFileItem {

    private final FileItem item;

    private final List<SyncObjecItem> objects;

    public SyncFileItem(@NotNull FileItem item, List<SyncObjecItem> objects) {
        this.item = item;
        this.objects = objects != null ? objects : List.of();
    }

    public FileItem getItem() {
        return item;
    }

    public List<SyncObjecItem> getObjects() {
        return objects;
    }
}
