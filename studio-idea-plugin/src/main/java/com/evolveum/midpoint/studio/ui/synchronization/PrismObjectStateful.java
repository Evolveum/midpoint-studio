package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

// todo rename
public class PrismObjectStateful<O extends ObjectType> {

    private PrismObject<O> original;

    private PrismObject<O> current;

    public PrismObjectStateful() {
        this(null);
    }

    public PrismObjectStateful(PrismObject<O> original) {
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

    public boolean isChanged() {
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
        PrismObject<O> newOriginal = current != null ? current.clone() : null;

        original = newOriginal;
    }
}
