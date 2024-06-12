package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.jetbrains.annotations.NotNull;

public class ApplicableObjectDelta<O extends ObjectType> implements ApplicableDelta<O> {

    private final ObjectDelta<O> objectDelta;

    public ApplicableObjectDelta(@NotNull ObjectDelta<O> objectDelta) {
        this.objectDelta = objectDelta;
    }

    @Override
    public ObjectDelta<O> getDelta(PrismObject<O> target) {
        return objectDelta;
    }
}
