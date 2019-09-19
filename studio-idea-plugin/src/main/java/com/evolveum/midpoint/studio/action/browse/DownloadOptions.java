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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadOptions that = (DownloadOptions) o;

        if (showOnly != that.showOnly) return false;
        return raw == that.raw;
    }

    @Override
    public int hashCode() {
        int result = (showOnly ? 1 : 0);
        result = 31 * result + (raw ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{showOnly=" + showOnly + ", raw=" + raw + '}';
    }
}
