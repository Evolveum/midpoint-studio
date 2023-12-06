package com.evolveum.midpoint.studio.impl.configuration;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CleanupConfiguration implements Serializable {

    private List<CleanupPathConfiguration> cleanupPaths;

    public @NotNull List<CleanupPathConfiguration> getCleanupPaths() {
        if (cleanupPaths == null) {
            cleanupPaths = new ArrayList<>();
        }
        return cleanupPaths;
    }

    public void setCleanupPaths(List<CleanupPathConfiguration> cleanupPaths) {
        this.cleanupPaths = cleanupPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CleanupConfiguration that = (CleanupConfiguration) o;
        return Objects.equals(cleanupPaths, that.cleanupPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cleanupPaths);
    }
}
