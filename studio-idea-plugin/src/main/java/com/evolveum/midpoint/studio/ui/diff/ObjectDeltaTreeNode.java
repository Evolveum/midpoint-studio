package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.delta.ItemDelta;

public record ObjectDeltaTreeNode(ItemDelta<?, ?> delta, Item<?, ?> targetItem) {
}
