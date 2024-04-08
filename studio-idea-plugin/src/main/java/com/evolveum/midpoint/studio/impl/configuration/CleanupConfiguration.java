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
    private CleanupPathActionConfiguration askActionOverride;

    private boolean cleanupConnectorReferences;

    private boolean replaceConnectorOidsWithFilter;

    private boolean warnAboutMissingReferences;

    private boolean removeContainerIds;

    private MissingRefObjects missingReferences;

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

    public boolean isCleanupConnectorReferences() {
        return cleanupConnectorReferences;
    }

    public void setCleanupConnectorReferences(boolean cleanupConnectorReferences) {
        this.cleanupConnectorReferences = cleanupConnectorReferences;
    }

    public boolean isReplaceConnectorOidsWithFilter() {
        return replaceConnectorOidsWithFilter;
    }

    public void setReplaceConnectorOidsWithFilter(boolean replaceConnectorOidsWithFilter) {
        this.replaceConnectorOidsWithFilter = replaceConnectorOidsWithFilter;
    }

    public boolean isWarnAboutMissingReferences() {
        return warnAboutMissingReferences;
    }

    public void setWarnAboutMissingReferences(boolean warnAboutMissingReferences) {
        this.warnAboutMissingReferences = warnAboutMissingReferences;
    }

    public boolean isRemoveContainerIds() {
        return removeContainerIds;
    }

    public void setRemoveContainerIds(boolean removeContainerIds) {
        this.removeContainerIds = removeContainerIds;
    }

    public MissingRefObjects getMissingReferences() {
        if (missingReferences == null) {
            missingReferences = new MissingRefObjects();
            missingReferences.setDefaultAction(MissingRefAction.DOWNLOAD);
        }
        return missingReferences;
    }

    public void setMissingReferences(MissingRefObjects missingReferences) {
        this.missingReferences = missingReferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CleanupConfiguration that = (CleanupConfiguration) o;
        return cleanupConnectorReferences == that.cleanupConnectorReferences
                && replaceConnectorOidsWithFilter == that.replaceConnectorOidsWithFilter
                && warnAboutMissingReferences == that.warnAboutMissingReferences
                && Objects.equals(cleanupPaths, that.cleanupPaths)
                && askActionOverride == that.askActionOverride
                && removeContainerIds == that.removeContainerIds
                && Objects.equals(missingReferences, that.missingReferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                cleanupPaths, askActionOverride, cleanupConnectorReferences, replaceConnectorOidsWithFilter,
                warnAboutMissingReferences, removeContainerIds, missingReferences);
    }

    public void copyTo(@NotNull CleanupConfiguration configuration) {
        configuration.setAskActionOverride(askActionOverride);

        List<CleanupPathConfiguration> paths = getCleanupPaths().stream()
                .map(CleanupPathConfiguration::copy)
                .toList();
        configuration.setCleanupPaths(paths);

        configuration.setCleanupConnectorReferences(cleanupConnectorReferences);
        configuration.setReplaceConnectorOidsWithFilter(replaceConnectorOidsWithFilter);
        configuration.setWarnAboutMissingReferences(warnAboutMissingReferences);
        configuration.setRemoveContainerIds(removeContainerIds);

        if (missingReferences != null) {
            configuration.setMissingReferences(missingReferences.copy());
        }
    }

    public CleanupConfiguration copy() {
        CleanupConfiguration clone = new CleanupConfiguration();
        copyTo(clone);

        return clone;
    }
}
