package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.client.MidPointObject;
import org.jetbrains.annotations.NotNull;

public class SynchronizationObjectItem extends SynchronizationItem {

    private final SynchronizationFileItem<?> fileItem;

    private String oid;
    private String name;
    private ObjectTypes type;

    private ModificationType modificationType;

    private MidPointObject local;
    private MidPointObject remote;

    private PrismObjectStateful<?> localObject;
    private PrismObjectStateful<?> remoteObject;

    public SynchronizationObjectItem(@NotNull SynchronizationFileItem<?> fileItem) {
        this(fileItem, null, null, null, null, null);
    }

    public SynchronizationObjectItem(
            SynchronizationFileItem<?> fileItem, String oid, String name, ObjectTypes type, MidPointObject local,
            MidPointObject remote) {

        super(SynchronizationItemType.OBJECT);

        this.fileItem = fileItem;
        this.oid = oid;
        this.name = name;
        this.type = type;
        this.local = local;
        this.remote = remote;
    }

    @NotNull
    public SynchronizationFileItem<?> getFileItem() {
        return fileItem;
    }

    @Override
    public String getName() {
        return name;
    }

    public MidPointObject getLocal() {
        return local;
    }

    public MidPointObject getRemote() {
        return remote;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }

    public void setModificationType(ModificationType modificationType) {
        this.modificationType = modificationType;
    }
}