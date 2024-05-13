package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.client.MidPointObject;

public class DiffObjectItem extends SynchronizationObjectItem {

    private MidPointObject base;

    private PrismObjectStateful<?> baseObject;

    public DiffObjectItem(SynchronizationFileItem<?> fileItem) {
        super(fileItem);
    }
}
