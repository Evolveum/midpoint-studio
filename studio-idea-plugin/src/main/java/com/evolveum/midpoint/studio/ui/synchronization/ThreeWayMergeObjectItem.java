package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.client.MidPointObject;
import org.jetbrains.annotations.NotNull;

public class ThreeWayMergeObjectItem extends SynchronizationObjectItem {

    private MidPointObject base;

    private PrismObjectStateful baseObject;

    public ThreeWayMergeObjectItem(@NotNull SynchronizationFileItem<?> fileItem) {
        super(fileItem);
    }
}
