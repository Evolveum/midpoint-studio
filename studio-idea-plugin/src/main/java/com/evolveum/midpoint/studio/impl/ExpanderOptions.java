package com.evolveum.midpoint.studio.impl;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExpanderOptions {

    private boolean expandEncrypted = true;

    public boolean expandEncrypted() {
        return expandEncrypted;
    }

    public ExpanderOptions expandEncrypted(boolean expand) {
        this.expandEncrypted = expand;

        return this;
    }
}
