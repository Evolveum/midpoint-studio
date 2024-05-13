package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.jetbrains.annotations.NotNull;

// todo rename
public class PrismObjectStateful<O extends ObjectType> {

    private PrismObject<O> original;

    private PrismObject<O> current;

    public PrismObjectStateful(@NotNull PrismObject<O> original) {
        this.original = original;
        this.current = original.clone();
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
        return original.equivalent(current);
    }

    public void revert() {
        setCurrent(original.clone());
    }

    public void commit() {
        original = current.clone();
    }
}
