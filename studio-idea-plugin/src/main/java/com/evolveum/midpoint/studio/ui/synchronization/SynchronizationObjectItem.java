package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SynchronizationObjectItem extends SynchronizationItem {

    private final SynchronizationFileItem<?> fileItem;

    private String oid;
    private String name;
    private ObjectTypes objectType;

    private MidPointObject local;
    private MidPointObject remote;

    private ModificationType modificationType;

    private PrismObjectStateful<?> localObject = new PrismObjectStateful<>();
    private PrismObjectStateful<?> remoteObject = new PrismObjectStateful<>();

    public SynchronizationObjectItem(
            @NotNull SynchronizationFileItem<?> fileItem, @NotNull String oid, @NotNull String name,
            @NotNull ObjectTypes type, MidPointObject local, MidPointObject remote) {

        super(SynchronizationItemType.OBJECT);

        this.fileItem = fileItem;
        this.oid = oid;
        this.name = name;
        this.objectType = type;
        this.local = local;
        this.remote = remote;
    }

    public void initialize() throws SchemaException, IOException {
        setupPrismStatefulObject(local, localObject);
        setupPrismStatefulObject(remote, remoteObject);
    }

    private void setupPrismStatefulObject(MidPointObject object, PrismObjectStateful<?> prismObjectStateful)
            throws SchemaException, IOException {

        if (object == null) {
            return;
        }

        PrismObject prismObject = ClientUtils.createParser(PrismContext.get(), object.getContent()).parse();
        prismObjectStateful.setCurrent(prismObject);
        prismObjectStateful.commit();
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

    public void setModificationType(@Nullable ModificationType modificationType) {
        this.modificationType = modificationType;
    }

    public PrismObjectStateful<?> getLocalObject() {
        return localObject;
    }

    public String getOid() {
        return oid;
    }

    public PrismObjectStateful<?> getRemoteObject() {
        return remoteObject;
    }

    @NotNull
    public ObjectTypes getObjectType() {
        return objectType;
    }
}