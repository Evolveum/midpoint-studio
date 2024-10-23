package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.jetbrains.annotations.NotNull;

public class ApplicableItemDelta<O extends ObjectType> implements ApplicableDelta<O> {

    private final ItemDelta<?, ?> itemDelta;

    public ApplicableItemDelta(@NotNull ItemDelta<?, ?> itemDelta) {
        this.itemDelta = itemDelta;
    }

    @Override
    public ObjectDelta<O> getDelta(PrismObject<O> target) {
        ObjectDelta<O> delta = target.createModifyDelta();
        delta.addModification(itemDelta.clone());

        return delta;
    }
}
