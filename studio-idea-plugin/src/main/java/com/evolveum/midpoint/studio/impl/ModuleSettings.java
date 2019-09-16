package com.evolveum.midpoint.studio.impl;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ModuleSettings {

    private MidPointSettings midPointSettings = MidPointSettings.createDefaultSettings();

    private EnvironmentSettings environmentSettings = EnvironmentSettings.createDefaultSettings();

    private FileObjectSettings fileObjectSettings = FileObjectSettings.createDefaultSettings();

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

    public FileObjectSettings getFileObjectSettings() {
        return fileObjectSettings;
    }

    public void setFileObjectSettings(FileObjectSettings fileObjectSettings) {
        this.fileObjectSettings = fileObjectSettings;
    }

    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleSettings that = (ModuleSettings) o;

        if (midPointSettings != null ? !midPointSettings.equals(that.midPointSettings) : that.midPointSettings != null)
            return false;
        if (environmentSettings != null ? !environmentSettings.equals(that.environmentSettings) : that.environmentSettings != null)
            return false;
        if (fileObjectSettings != null ? !fileObjectSettings.equals(that.fileObjectSettings) : that.fileObjectSettings != null)
            return false;
        return masterPassword != null ? masterPassword.equals(that.masterPassword) : that.masterPassword == null;
    }

    @Override
    public int hashCode() {
        int result = midPointSettings != null ? midPointSettings.hashCode() : 0;
        result = 31 * result + (environmentSettings != null ? environmentSettings.hashCode() : 0);
        result = 31 * result + (fileObjectSettings != null ? fileObjectSettings.hashCode() : 0);
        result = 31 * result + (masterPassword != null ? masterPassword.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ModuleSettings{" +
                "midPointSettings=" + midPointSettings +
                ", environmentSettings=" + environmentSettings +
                ", fileObjectSettings=" + fileObjectSettings +
                ", masterPassword='" + (masterPassword != null ? StringUtils.repeat('*', masterPassword.length()) : "NULL") + '\'' +
                '}';
    }
}
