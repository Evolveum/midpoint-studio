package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.ui.diff.ApplicableDelta;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SynchronizationObjectItem extends SynchronizationItem {

    private final SynchronizationFileItem<?> fileItem;

    private String oid;
    private String name;
    private ObjectTypes objectType;

    private MidPointObject local;
    private MidPointObject remote;

    private PrismObjectHolder<?> localObject = new PrismObjectHolder<>();
    private PrismObjectHolder<?> remoteObject = new PrismObjectHolder<>();

    private List<ApplicableDelta<?>> ignoredLocalDeltas = new ArrayList<>();
    private List<ApplicableDelta<?>> ignoredRemoteDeltas = new ArrayList<>();

    /**
     * Hash codes and boolean flag which indicates whether the objects are the same.
     * It's cached, so we don't have to calculate delta/diff every time tree node is rendered.
     */
    private int currentLocalObjectHash;
    private int currentRemoteObjectHash;
    private boolean currentIsUnchanged;

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

    private void setupPrismStatefulObject(MidPointObject object, PrismObjectHolder<?> prismObjectStateful)
            throws SchemaException, IOException {

        if (object == null) {
            return;
        }

        String content = object.getContent();
        PrismObject prismObject = ClientUtils.createParser(PrismContext.get(), content).parse();

        prismObjectStateful.setCurrent(prismObject);
        prismObjectStateful.commit();
    }

    @Override
    public boolean hasLocalChanges() {
        return localObject.hasChanges();
    }

    @Override
    public boolean hasRemoteChanges() {
        return remoteObject.hasChanges();
    }

    @Override
    public boolean isNew() {
        return remoteObject.getOriginal() == null;
    }

    @Override
    public boolean isUnchanged() {
        if (localObject == null || remoteObject == null) {
            return false;
        }

        PrismObject<?> local = localObject.getCurrent();
        PrismObject remote = remoteObject.getCurrent();

        if (currentLocalObjectHash == local.hashCode() && currentRemoteObjectHash == remote.hashCode()) {
            return currentIsUnchanged;
        }

        currentLocalObjectHash = local.hashCode();
        currentRemoteObjectHash = remote.hashCode();

        currentIsUnchanged = local.diff(remote).isEmpty();

        return currentIsUnchanged;
    }

    // todo not correct, don't compare local/remote current, check whether there are deltas still to be resolved (applied/ignored - both ways)
    @Override
    public boolean isVisible() {
        return localObject.hasChanges() || remoteObject.hasChanges() || localObject.currentEquivalent(remoteObject);
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

    public PrismObjectHolder<?> getLocalObject() {
        return localObject;
    }

    public String getOid() {
        return oid;
    }

    public PrismObjectHolder<?> getRemoteObject() {
        return remoteObject;
    }

    @NotNull
    public ObjectTypes getObjectType() {
        return objectType;
    }

    public List<ApplicableDelta<?>> getIgnoredLocalDeltas() {
        return ignoredLocalDeltas;
    }

    public List<ApplicableDelta<?>> getIgnoredRemoteDeltas() {
        return ignoredRemoteDeltas;
    }
}