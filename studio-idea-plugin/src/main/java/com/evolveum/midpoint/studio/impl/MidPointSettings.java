package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.ObjectTypesConverter;
import com.intellij.util.xmlb.annotations.OptionTag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointSettings implements Serializable {

    private String projectId;

    private String midPointVersion;

    private String dowloadFilePattern;

    private String generatedFilePattern;

    private boolean printRestCommunicationToConsole;

    private DocGeneratorOptions docGeneratorOptions;

    @OptionTag(converter = ObjectTypesConverter.class)
    private List<ObjectTypes> downloadTypesInclude;

    @OptionTag(converter = ObjectTypesConverter.class)
    private List<ObjectTypes> downloadTypesExclude;

    private int typesToDownloadLimit;

    private int restResponseTimeout = 60;

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

    public String getDowloadFilePattern() {
        return dowloadFilePattern;
    }

    public void setDowloadFilePattern(String dowloadFilePattern) {
        this.dowloadFilePattern = dowloadFilePattern;
    }

    public String getGeneratedFilePattern() {
        return generatedFilePattern;
    }

    public boolean isPrintRestCommunicationToConsole() {
        return printRestCommunicationToConsole;
    }

    public void setPrintRestCommunicationToConsole(boolean printRestCommunicationToConsole) {
        this.printRestCommunicationToConsole = printRestCommunicationToConsole;
    }

    public void setGeneratedFilePattern(String generatedFilePattern) {
        this.generatedFilePattern = generatedFilePattern;
    }

    public DocGeneratorOptions getDocGeneratorOptions() {
        return docGeneratorOptions;
    }

    public void setDocGeneratorOptions(DocGeneratorOptions docGeneratorOptions) {
        this.docGeneratorOptions = docGeneratorOptions;
    }

    public List<ObjectTypes> getDownloadTypesInclude() {
        if (downloadTypesInclude == null) {
            downloadTypesInclude = new ArrayList<>();
        }
        return downloadTypesInclude;
    }

    public void setDownloadTypesInclude(List<ObjectTypes> downloadTypesInclude) {
        this.downloadTypesInclude = downloadTypesInclude;
    }

    public List<ObjectTypes> getDownloadTypesExclude() {
        if (downloadTypesExclude == null) {
            downloadTypesExclude = new ArrayList<>();
        }
        return downloadTypesExclude;
    }

    public void setDownloadTypesExclude(List<ObjectTypes> downloadTypesExclude) {
        this.downloadTypesExclude = downloadTypesExclude;
    }

    public int getTypesToDownloadLimit() {
        return typesToDownloadLimit;
    }

    public void setTypesToDownloadLimit(int typesToDownloadLimit) {
        this.typesToDownloadLimit = typesToDownloadLimit;
    }

    public int getRestResponseTimeout() {
        return restResponseTimeout;
    }

    public void setRestResponseTimeout(int restResponseTimeout) {
        this.restResponseTimeout = restResponseTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MidPointSettings settings = (MidPointSettings) o;

        if (printRestCommunicationToConsole != settings.printRestCommunicationToConsole) return false;
        if (typesToDownloadLimit != settings.typesToDownloadLimit) return false;
        if (restResponseTimeout != settings.restResponseTimeout) return false;
        if (projectId != null ? !projectId.equals(settings.projectId) : settings.projectId != null) return false;
        if (midPointVersion != null ? !midPointVersion.equals(settings.midPointVersion) : settings.midPointVersion != null)
            return false;
        if (dowloadFilePattern != null ? !dowloadFilePattern.equals(settings.dowloadFilePattern) : settings.dowloadFilePattern != null)
            return false;
        if (generatedFilePattern != null ? !generatedFilePattern.equals(settings.generatedFilePattern) : settings.generatedFilePattern != null)
            return false;
        if (docGeneratorOptions != null ? !docGeneratorOptions.equals(settings.docGeneratorOptions) : settings.docGeneratorOptions != null)
            return false;
        if (downloadTypesInclude != null ? !downloadTypesInclude.equals(settings.downloadTypesInclude) : settings.downloadTypesInclude != null)
            return false;
        return downloadTypesExclude != null ? downloadTypesExclude.equals(settings.downloadTypesExclude) : settings.downloadTypesExclude == null;
    }

    @Override
    public int hashCode() {
        int result = projectId != null ? projectId.hashCode() : 0;
        result = 31 * result + (midPointVersion != null ? midPointVersion.hashCode() : 0);
        result = 31 * result + (dowloadFilePattern != null ? dowloadFilePattern.hashCode() : 0);
        result = 31 * result + (generatedFilePattern != null ? generatedFilePattern.hashCode() : 0);
        result = 31 * result + (printRestCommunicationToConsole ? 1 : 0);
        result = 31 * result + (docGeneratorOptions != null ? docGeneratorOptions.hashCode() : 0);
        result = 31 * result + (downloadTypesInclude != null ? downloadTypesInclude.hashCode() : 0);
        result = 31 * result + (downloadTypesExclude != null ? downloadTypesExclude.hashCode() : 0);
        result = 31 * result + typesToDownloadLimit;
        result = 31 * result + restResponseTimeout;
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
        settings.setMidPointVersion("4.1-SNAPSHOT");

        settings.setDowloadFilePattern("objects/$T/$n.xml");
        settings.setGeneratedFilePattern("scratches/gen/$n.xml");

        List<ObjectTypes> types = new ArrayList<>();
        types.addAll(Arrays.asList(new ObjectTypes[]{
                ObjectTypes.USER,
                ObjectTypes.SHADOW,
                ObjectTypes.CASE,
                ObjectTypes.REPORT_DATA,
                ObjectTypes.CONNECTOR,
                ObjectTypes.ACCESS_CERTIFICATION_CAMPAIGN,
                ObjectTypes.NODE
        }));
        settings.setDownloadTypesExclude(types);
        settings.setTypesToDownloadLimit(100);


        return settings;
    }
}
