package com.evolveum.midpoint.studio.impl.client;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteOptions {

    private boolean raw;

    public boolean raw() {
        return this.raw;
    }

    public DeleteOptions raw(final boolean raw) {
        this.raw = raw;
        return this;
    }
}
