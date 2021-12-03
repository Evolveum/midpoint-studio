package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.client.AddOptions;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadOptions {

    private boolean overwrite;

    private boolean raw;

    private boolean testConnection;

    public boolean overwrite() {
        return this.overwrite;
    }

    public boolean raw() {
        return this.raw;
    }

    public boolean testConnection() {
        return this.testConnection;
    }

    public UploadOptions overwrite(final boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public UploadOptions raw(final boolean raw) {
        this.raw = raw;
        return this;
    }

    public UploadOptions testConnection(final boolean testConnection) {
        this.testConnection = testConnection;
        return this;
    }

    public AddOptions buildAddOptions() {
        return new AddOptions().overwrite(overwrite).raw(raw);
    }

    @Override
    public String toString() {
        return "{" +
                "overwrite=" + overwrite +
                ", raw=" + raw +
                ", testConnection=" + testConnection +
                '}';
    }
}
