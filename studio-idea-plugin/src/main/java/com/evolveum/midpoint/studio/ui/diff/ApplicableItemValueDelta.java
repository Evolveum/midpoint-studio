package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class ApplicableItemValueDelta<O extends ObjectType> implements ApplicableDelta<O> {

    private final ItemDelta<?, ?> itemDelta;

    private final ModificationType modificationType;

    private final PrismValue value;

    public ApplicableItemValueDelta(ItemDelta<?, ?> itemDelta, ModificationType modificationType, PrismValue value) {
        this.itemDelta = itemDelta;
        this.modificationType = modificationType;
        this.value = value;
    }

    @Override
    public ObjectDelta<O> getDelta(PrismObject<O> target) {
        PrismValue cloned = value.clone();

        ItemDelta itemDelta = this.itemDelta.clone();
        itemDelta.clear();
        switch (modificationType) {
            case REPLACE -> itemDelta.addValueToReplace(cloned);
            case ADD -> itemDelta.addValueToAdd(cloned);
            case DELETE -> itemDelta.addValueToDelete(cloned);
        }

        ObjectDelta<O> delta = target.createModifyDelta();
        delta.addModification(itemDelta);

        return delta;
    }
}
