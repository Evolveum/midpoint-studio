package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationUtil;

import java.util.HashSet;
import java.util.Set;

public abstract class ObjectDeltaTreeNode<T> {

    private T value;

    public ObjectDeltaTreeNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    abstract String getText();

    abstract ModificationType getModificationType();

    abstract ApplicableDelta<?> getApplicableDelta();

    protected ModificationType getModificationType(ItemDelta<?, ?> delta) {
        Set<ModificationType> modifications = new HashSet<>();
        if (delta.getValuesToAdd() != null && !delta.getValuesToAdd().isEmpty()) {
            modifications.add(ModificationType.ADD);
        }
        if (delta.getValuesToDelete() != null && !delta.getValuesToDelete().isEmpty()) {
            modifications.add(ModificationType.DELETE);
        }
        if (delta.getValuesToReplace() != null && !delta.getValuesToReplace().isEmpty()) {
            modifications.add(ModificationType.REPLACE);
        }

        return SynchronizationUtil.getModificationType(modifications);
    }
}
