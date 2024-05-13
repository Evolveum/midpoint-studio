package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.client.MidPointObject;

public class ThreeWayMergeObjectItem extends SynchronizationObjectItem {

    private MidPointObject base;

    private PrismObjectStateful baseObject;

    public ThreeWayMergeObjectItem(SynchronizationFileItem<?> fileItem, String oid, String name, ObjectTypes type,
                                   MidPointObject local, MidPointObject remote, MidPointObject base) {

        super(fileItem, oid, name, type, local, remote);

        this.base = base;
    }
}
