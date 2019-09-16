package com.evolveum.midpoint.studio.action.browse;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DownloadOptions {

    private boolean showOnly;
    private boolean raw;

    public boolean showOnly() {
        return this.showOnly;
    }

    public boolean raw() {
        return this.raw;
    }

    public DownloadOptions showOnly(final boolean showOnly) {
        this.showOnly = showOnly;
        return this;
    }

    public DownloadOptions raw(final boolean raw) {
        this.raw = raw;
        return this;
    }
}
