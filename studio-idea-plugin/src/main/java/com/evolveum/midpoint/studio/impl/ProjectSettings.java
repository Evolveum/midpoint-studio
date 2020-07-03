package com.evolveum.midpoint.studio.impl;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProjectSettings {

    private MidPointSettings midPointSettings = MidPointSettings.createDefaultSettings();

    private EnvironmentSettings environmentSettings = EnvironmentSettings.createDefaultSettings();

    private String oldMasterPassword;

    private String masterPassword;

    public MidPointSettings getMidPointSettings() {
        return midPointSettings;
    }

    public void setMidPointSettings(MidPointSettings midPointSettings) {
        this.midPointSettings = midPointSettings;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        return environmentSettings;
    }

    public void setEnvironmentSettings(EnvironmentSettings environmentSettings) {
        this.environmentSettings = environmentSettings;
    }

    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    public String getOldMasterPassword() {
        return oldMasterPassword;
    }

    public void setOldMasterPassword(String oldMasterPassword) {
        this.oldMasterPassword = oldMasterPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectSettings settings = (ProjectSettings) o;

        if (midPointSettings != null ? !midPointSettings.equals(settings.midPointSettings) : settings.midPointSettings != null)
            return false;
        if (environmentSettings != null ? !environmentSettings.equals(settings.environmentSettings) : settings.environmentSettings != null)
            return false;
        if (oldMasterPassword != null ? !oldMasterPassword.equals(settings.oldMasterPassword) : settings.oldMasterPassword != null)
            return false;
        return masterPassword != null ? masterPassword.equals(settings.masterPassword) : settings.masterPassword == null;
    }

    @Override
    public int hashCode() {
        int result = midPointSettings != null ? midPointSettings.hashCode() : 0;
        result = 31 * result + (environmentSettings != null ? environmentSettings.hashCode() : 0);
        result = 31 * result + (oldMasterPassword != null ? oldMasterPassword.hashCode() : 0);
        result = 31 * result + (masterPassword != null ? masterPassword.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ModuleSettings{" +
                "midPointSettings=" + midPointSettings +
                ", environmentSettings=" + environmentSettings +
                ", masterPassword='" + (masterPassword != null ? StringUtils.repeat('*', masterPassword.length()) : "NULL") + '\'' +
                '}';
    }
}
