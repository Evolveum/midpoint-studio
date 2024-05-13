package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class PrismObjectHolder<O extends ObjectType> {

    private PrismObject<O> original;

    private PrismObject<O> current;

    public PrismObjectHolder() {
        this(null);
    }

    public PrismObjectHolder(PrismObject<O> original) {
        this.original = original;
        this.current = original != null ? original.clone() : null;
    }

    public PrismObject<O> getCurrent() {
        return current;
    }

    public PrismObject<O> getOriginal() {
        return original;
    }

    public void setCurrent(PrismObject<O> current) {
        this.current = current;
    }

    public boolean hasChanges() {
        if (original == null) {
            return current != null;
        }

        return !original.equivalent(current);
    }

    public void revert() {
        PrismObject<O> newCurrent = original != null ? original.clone() : null;

        setCurrent(newCurrent);
    }

    public void commit() {
        original = current != null ? current.clone() : null;
    }

    public boolean currentEquivalent(PrismObjectHolder<?> other) {
        if (other == null) {
            return false;
        }

        if (current == null && other.current == null) {
            return true;
        }

        if (current == null || other.current == null) {
            return false;
        }

        return current.equivalent(other.current);
    }
}
