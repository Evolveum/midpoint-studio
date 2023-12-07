package com.evolveum.midpoint.studio.impl.configuration;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CleanupConfiguration implements Serializable {

    private List<CleanupPathConfiguration> cleanupPaths;

    /**
     * Default action to be used when user should be asked what to do with path - correct values are:
     * {@link CleanupPathActionConfiguration#IGNORE} or {@link CleanupPathActionConfiguration#REMOVE}
     */
    private CleanupPathActionConfiguration askActionOverride = CleanupPathActionConfiguration.REMOVE;

    public @NotNull List<CleanupPathConfiguration> getCleanupPaths() {
        if (cleanupPaths == null) {
            cleanupPaths = new ArrayList<>();
        }
        return cleanupPaths;
    }

    public void setCleanupPaths(List<CleanupPathConfiguration> cleanupPaths) {
        this.cleanupPaths = cleanupPaths;
    }

    public CleanupPathActionConfiguration getAskActionOverride() {
        return askActionOverride;
    }

    public void setAskActionOverride(CleanupPathActionConfiguration askActionOverride) {
        this.askActionOverride = askActionOverride;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CleanupConfiguration that = (CleanupConfiguration) o;
        return Objects.equals(cleanupPaths, that.cleanupPaths) && askActionOverride == that.askActionOverride;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cleanupPaths, askActionOverride);
    }
}
