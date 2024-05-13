package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Expander;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SynchronizationObjectItem extends SynchronizationItem {

    private final SynchronizationFileItem<?> fileItem;

    private String oid;
    private String name;
    private ObjectTypes objectType;

    private MidPointObject local;
    private MidPointObject remote;

    private PrismObjectHolder<?> localObject = new PrismObjectHolder<>();
    private PrismObjectHolder<?> remoteObject = new PrismObjectHolder<>();

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

    public void initialize(Expander expander) throws SchemaException, IOException {
        setupPrismStatefulObject(local, localObject, expander);
        setupPrismStatefulObject(remote, remoteObject, null);
    }

    private void setupPrismStatefulObject(MidPointObject object, PrismObjectHolder<?> prismObjectStateful, Expander expander)
            throws SchemaException, IOException {

        if (object == null) {
            return;
        }

        String content = object.getContent();
        if (expander != null) {
            VirtualFile file = object.getFile() != null ? VfsUtil.findFileByIoFile(object.getFile(), true) : null;

            content = expander.expand(object.getContent(), file);
        }

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
}