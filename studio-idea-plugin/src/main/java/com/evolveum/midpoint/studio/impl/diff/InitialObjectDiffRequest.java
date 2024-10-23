package com.evolveum.midpoint.studio.impl.diff;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class InitialObjectDiffRequest<O extends ObjectType> {

    private PrismObject<O> previousInitialObject;

    private PrismObject<O> currentObject;

    private PrismObject<O> result;

    /**
     * Previous vs current vanilla initial object changes.
     */
    private ObjectDelta<O> initialObjectChanges;

    /**
     * Current object changes created as a diff between previous vanilla initial object
     * and current state of the object.
     */
    private ObjectDelta<O> currentObjectChanges;

    public InitialObjectDiffRequest(
            PrismObject<O> previousInitialObject, PrismObject<O> currentObject, ObjectDelta<O> initialObjectChanges,
            ObjectDelta<O> currentObjectChanges) {

        this.previousInitialObject = previousInitialObject;
        this.currentObject = currentObject;
        this.initialObjectChanges = initialObjectChanges;
        this.currentObjectChanges = currentObjectChanges;
    }
}
