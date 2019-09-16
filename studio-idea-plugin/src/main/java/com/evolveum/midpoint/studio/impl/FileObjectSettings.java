package com.evolveum.midpoint.studio.impl;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class FileObjectSettings implements Serializable {

    private String dowloadFilePattern;

    private String generatedFilePattern;

    public static FileObjectSettings createDefaultSettings() {
        FileObjectSettings settings = new FileObjectSettings();
        settings.setDowloadFilePattern("objects/$T/$n.xml");
        settings.setGeneratedFilePattern("scratches/gen/$n.xml");

        return settings;
    }

    public String getDowloadFilePattern() {
        return dowloadFilePattern;
    }

    public void setDowloadFilePattern(String dowloadFilePattern) {
        this.dowloadFilePattern = dowloadFilePattern;
    }

    public String getGeneratedFilePattern() {
        return generatedFilePattern;
    }

    public void setGeneratedFilePattern(String generatedFilePattern) {
        this.generatedFilePattern = generatedFilePattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileObjectSettings settings = (FileObjectSettings) o;

        if (dowloadFilePattern != null ? !dowloadFilePattern.equals(settings.dowloadFilePattern) : settings.dowloadFilePattern != null)
            return false;
        return generatedFilePattern != null ? generatedFilePattern.equals(settings.generatedFilePattern) : settings.generatedFilePattern == null;
    }

    @Override
    public int hashCode() {
        int result = dowloadFilePattern != null ? dowloadFilePattern.hashCode() : 0;
        result = 31 * result + (generatedFilePattern != null ? generatedFilePattern.hashCode() : 0);
        return result;
    }
}
