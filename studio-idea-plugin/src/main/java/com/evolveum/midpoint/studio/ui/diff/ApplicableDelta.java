package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public interface ApplicableDelta<O extends ObjectType> {

    ObjectDelta<O> getDelta(PrismObject<O> target);
}
