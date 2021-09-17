package com.evolveum.midpoint.studio.client;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AddOptions {

    private boolean force;

    private boolean raw;

    private boolean overwrite;

    private boolean isImport;

    private boolean reevaluateSearchFilters;

    public boolean force() {
        return this.force;
    }

    public boolean raw() {
        return this.raw;
    }

    public boolean overwrite() {
        return this.overwrite;
    }

    public boolean isImport() {
        return this.isImport;
    }

    public boolean reevaluateSearchFilters() {
        return this.reevaluateSearchFilters;
    }

    public AddOptions force(final boolean force) {
        this.force = force;
        return this;
    }

    public AddOptions raw(final boolean raw) {
        this.raw = raw;
        return this;
    }

    public AddOptions overwrite(final boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public AddOptions isImport(final boolean isImport) {
        this.isImport = isImport;
        return this;
    }

    public AddOptions reevaluateSearchFilters(final boolean reevaluateSearchFilters) {
        this.reevaluateSearchFilters = reevaluateSearchFilters;
        return this;
    }
}
