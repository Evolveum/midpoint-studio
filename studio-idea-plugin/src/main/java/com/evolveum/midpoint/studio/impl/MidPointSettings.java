package com.evolveum.midpoint.studio.impl;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointSettings implements Serializable {

    private String projectId;

    private String midPointVersion;

    public MidPointSettings() {
    }

    public String getMidPointVersion() {
        return midPointVersion;
    }

    public void setMidPointVersion(String midPointVersion) {
        this.midPointVersion = midPointVersion;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MidPointSettings settings = (MidPointSettings) o;

        if (projectId != null ? !projectId.equals(settings.projectId) : settings.projectId != null) return false;
        return midPointVersion != null ? midPointVersion.equals(settings.midPointVersion) : settings.midPointVersion == null;
    }

    @Override
    public int hashCode() {
        int result = projectId != null ? projectId.hashCode() : 0;
        result = 31 * result + (midPointVersion != null ? midPointVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MidPointSettings{" +
                "projectId='" + projectId + '\'' +
                ", midPointVersion='" + midPointVersion + '\'' +
                '}';
    }

    public static MidPointSettings createDefaultSettings() {
        MidPointSettings settings = new MidPointSettings();
        settings.setProjectId(UUID.randomUUID().toString());
        settings.setMidPointVersion("4.0-SNAPSHOT");

        return settings;
    }
}
