package com.evolveum.midpoint.studio.ui.synchronization;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class SynchronizationItem {

    private String id = UUID.randomUUID().toString();

    private SynchronizationItemType type;

    public SynchronizationItem(@NotNull SynchronizationItemType type) {
        this.type = type;
    }

    @NotNull
    public SynchronizationItemType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public abstract String getName();

    public abstract boolean isVisible();

    public abstract boolean hasLocalChanges();

    public abstract boolean hasRemoteChanges();

    public abstract boolean isNew();
}
