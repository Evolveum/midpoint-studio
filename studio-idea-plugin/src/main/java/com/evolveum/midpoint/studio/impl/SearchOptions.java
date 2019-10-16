package com.evolveum.midpoint.studio.impl;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SearchOptions {

    private boolean raw;

    public boolean raw() {
        return this.raw;
    }

    public SearchOptions raw(final boolean raw) {
        this.raw = raw;
        return this;
    }


}
