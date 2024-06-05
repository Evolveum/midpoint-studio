package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.Set;
import java.util.stream.Collectors;

public class AllTreeNode<O extends ObjectType> extends ObjectDeltaTreeNode<ObjectDeltaTreeData<O>> {

    public AllTreeNode(ObjectDeltaTreeData<O> value) {
        super(value);
    }

    @Override
    String getText() {
        return "All";
    }

    @Override
    ModificationType getModificationType() {
        ObjectDeltaTreeData<O> delta = getValue();
        Set<ModificationType> set = delta.delta().getModifications().stream()
                .map(this::getModificationType)
                .collect(Collectors.toSet());

        return SynchronizationUtil.getModificationType(set);
    }
}
