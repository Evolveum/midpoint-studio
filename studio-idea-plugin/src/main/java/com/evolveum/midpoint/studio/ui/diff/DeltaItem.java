package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.PrismValue;

public record DeltaItem(ModificationType modificationType, PrismValue value) {

}
