package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;

public record DeltaItem(ItemDelta<?, ?> parent, ModificationType modificationType, PrismValue value) {

}
