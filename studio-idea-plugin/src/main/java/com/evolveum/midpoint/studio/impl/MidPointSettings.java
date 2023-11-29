package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.ObjectTypesListConverter;
import com.intellij.util.xmlb.annotations.OptionTag;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointSettings implements Serializable {

    private static final List<ObjectTypes> DEFAULT_DOWNLOAD_EXCLUDE;

    static {
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

        DEFAULT_DOWNLOAD_EXCLUDE = Collections.unmodifiableList(types);
    }

    private String projectId;

    private String dowloadFilePattern;

    private String generatedFilePattern;

    private boolean printRestCommunicationToConsole;

    private DocGeneratorOptions docGeneratorOptions;

    private boolean askToAddMidpointFacet = true;

    private boolean askToValidateEnvironmentCredentials = true;

    private boolean ignoreMissingKeys;

    @OptionTag(converter = ObjectTypesListConverter.class)
    private List<ObjectTypes> downloadTypesInclude;

    @OptionTag(converter = ObjectTypesListConverter.class)
    private List<ObjectTypes> downloadTypesExclude;

    private int typesToDownloadLimit;

    private int restResponseTimeout = 60;

    public MidPointSettings() {
    }

    public boolean isAskToAddMidpointFacet() {
        return askToAddMidpointFacet;
    }

    public void setAskToAddMidpointFacet(boolean askToAddMidpointFacet) {
        this.askToAddMidpointFacet = askToAddMidpointFacet;
    }

    public boolean isAskToValidateEnvironmentCredentials() {
        return askToValidateEnvironmentCredentials;
    }

    public void setAskToValidateEnvironmentCredentials(boolean askToValidateEnvironmentCredentials) {
        this.askToValidateEnvironmentCredentials = askToValidateEnvironmentCredentials;
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

    public boolean isIgnoreMissingKeys() {
        return ignoreMissingKeys;
    }

    public void setIgnoreMissingKeys(boolean ignoreMissingKeys) {
        this.ignoreMissingKeys = ignoreMissingKeys;
    }

    public MidPointSettings copy() {
        MidPointSettings other = new MidPointSettings();
        other.projectId = projectId;
        other.dowloadFilePattern = dowloadFilePattern;
        other.generatedFilePattern = generatedFilePattern;
        other.printRestCommunicationToConsole = printRestCommunicationToConsole;
        other.askToAddMidpointFacet = askToAddMidpointFacet;
        other.askToValidateEnvironmentCredentials = askToValidateEnvironmentCredentials;
        other.downloadTypesExclude = downloadTypesExclude != null ? new ArrayList<>(downloadTypesExclude) : null;
        other.downloadTypesInclude = downloadTypesInclude != null ? new ArrayList<>(downloadTypesInclude) : null;
        other.typesToDownloadLimit = typesToDownloadLimit;
        other.restResponseTimeout = restResponseTimeout;
        other.ignoreMissingKeys = ignoreMissingKeys;

        return other;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MidPointSettings that = (MidPointSettings) o;

        if (printRestCommunicationToConsole != that.printRestCommunicationToConsole) return false;
        if (askToAddMidpointFacet != that.askToAddMidpointFacet) return false;
        if (askToValidateEnvironmentCredentials != that.askToValidateEnvironmentCredentials) return false;
        if (ignoreMissingKeys != that.ignoreMissingKeys) return false;
        if (typesToDownloadLimit != that.typesToDownloadLimit) return false;
        if (restResponseTimeout != that.restResponseTimeout) return false;
        if (!Objects.equals(projectId, that.projectId)) return false;
        if (!Objects.equals(dowloadFilePattern, that.dowloadFilePattern))
            return false;
        if (!Objects.equals(generatedFilePattern, that.generatedFilePattern))
            return false;
        if (!Objects.equals(docGeneratorOptions, that.docGeneratorOptions))
            return false;
        if (!Objects.equals(downloadTypesInclude, that.downloadTypesInclude))
            return false;
        return Objects.equals(downloadTypesExclude, that.downloadTypesExclude);
    }

    @Override
    public int hashCode() {
        int result = projectId != null ? projectId.hashCode() : 0;
        result = 31 * result + (dowloadFilePattern != null ? dowloadFilePattern.hashCode() : 0);
        result = 31 * result + (generatedFilePattern != null ? generatedFilePattern.hashCode() : 0);
        result = 31 * result + (printRestCommunicationToConsole ? 1 : 0);
        result = 31 * result + (docGeneratorOptions != null ? docGeneratorOptions.hashCode() : 0);
        result = 31 * result + (askToAddMidpointFacet ? 1 : 0);
        result = 31 * result + (askToValidateEnvironmentCredentials ? 1 : 0);
        result = 31 * result + (ignoreMissingKeys ? 1 : 0);
        result = 31 * result + (downloadTypesInclude != null ? downloadTypesInclude.hashCode() : 0);
        result = 31 * result + (downloadTypesExclude != null ? downloadTypesExclude.hashCode() : 0);
        result = 31 * result + typesToDownloadLimit;
        result = 31 * result + restResponseTimeout;
        return result;
    }

    @Override
    public String toString() {
        return "MidPointSettings{projectId='" + projectId + "'}";
    }

    public static MidPointSettings createDefaultSettings() {
        MidPointSettings settings = new MidPointSettings();
        settings.setProjectId(UUID.randomUUID().toString());

        settings.setDowloadFilePattern("objects/$T/$n.xml");
        settings.setGeneratedFilePattern("scratches/gen/$n.xml");

        settings.setDownloadTypesExclude(new ArrayList<>(DEFAULT_DOWNLOAD_EXCLUDE));
        settings.setTypesToDownloadLimit(100);

        return settings;
    }
}
