package com.evolveum.midpoint.studio.ui.synchronization;

import org.jetbrains.annotations.NotNull;

public abstract class SynchronizationItem {

    private SynchronizationItemType type;

    private boolean visible = true;

    private boolean localChanges;

    private boolean remoteChanges;

    public SynchronizationItem(@NotNull SynchronizationItemType type) {
        this.type = type;
    }

    public abstract String getName();

    public boolean isLocalChanges() {
        return localChanges;
    }

    public void setLocalChanges(boolean localChanges) {
        this.localChanges = localChanges;
    }

    public boolean isRemoteChanges() {
        return remoteChanges;
    }

    public void setRemoteChanges(boolean remoteChanges) {
        this.remoteChanges = remoteChanges;
    }

    @NotNull
    public SynchronizationItemType getType() {
        return type;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
