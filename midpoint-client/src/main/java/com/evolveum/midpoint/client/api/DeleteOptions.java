package com.evolveum.midpoint.client.api;

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
