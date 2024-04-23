package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.studio.client.MidPointObject;
import org.jetbrains.annotations.NotNull;

public class SynchronizationObject {

    private final ObjectItem item;

    private MidPointObject localObject;

    private MidPointObject remoteObject;

    private ModificationType modificationType;

    public SynchronizationObject(@NotNull ObjectItem item) {
        this.item = item;

        this.localObject = item.local() != null ? item.local().copy() : null;
        this.remoteObject = item.remote() != null ? item.remote().copy() : null;
    }

    public ObjectItem getItem() {
        return item;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }

    public void setModificationType(ModificationType modificationType) {
        this.modificationType = modificationType;
    }

    public MidPointObject getLocalObject() {
        return localObject;
    }

    public MidPointObject getRemoteObject() {
        return remoteObject;
    }
}
