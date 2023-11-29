package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.DocGeneratorOptions;
import com.evolveum.midpoint.studio.util.ObjectTypesListConverter;
import com.intellij.util.xmlb.annotations.OptionTag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointConfiguration implements Serializable {

    private static final List<ObjectTypes> DEFAULT_DOWNLOAD_EXCLUDE;

    static {
        DEFAULT_DOWNLOAD_EXCLUDE = List.of(
                ObjectTypes.USER,
                ObjectTypes.SHADOW,
                ObjectTypes.CASE,
                ObjectTypes.REPORT_DATA,
                ObjectTypes.CONNECTOR,
                ObjectTypes.ACCESS_CERTIFICATION_CAMPAIGN,
                ObjectTypes.NODE
        );
    }

    private String projectId;

    private String downloadFilePattern = "objects/$T/$n.xml";

    private String generatedFilePattern = "scratches/gen/$n.xml";

    private Integer restClientTimeout = 60;

    private Boolean restLogCommunication = false;

    @OptionTag(converter = ObjectTypesListConverter.class)
    private List<ObjectTypes> downloadTypesInclude = new ArrayList<>();

    @OptionTag(converter = ObjectTypesListConverter.class)
    private List<ObjectTypes> downloadTypesExclude = new ArrayList<>();

    private Integer downloadLimit = 100;

    private DocGeneratorOptions docGeneratorOptions;

    private boolean askToAddMidpointFacet = true;

    private boolean askToValidateEnvironmentCredentials = true;

    private boolean ignoreMissingKeys;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDownloadFilePattern() {
        return downloadFilePattern;
    }

    public void setDownloadFilePattern(String downloadFilePattern) {
        this.downloadFilePattern = downloadFilePattern;
    }

    public String getGeneratedFilePattern() {
        return generatedFilePattern;
    }

    public void setGeneratedFilePattern(String generatedFilePattern) {
        this.generatedFilePattern = generatedFilePattern;
    }

    public Integer getRestClientTimeout() {
        return restClientTimeout;
    }

    public void setRestClientTimeout(Integer restClientTimeout) {
        this.restClientTimeout = restClientTimeout;
    }

    public Boolean getRestLogCommunication() {
        return restLogCommunication;
    }

    public void setRestLogCommunication(Boolean restLogCommunication) {
        this.restLogCommunication = restLogCommunication;
    }

    public List<ObjectTypes> getDownloadTypesInclude() {
        return downloadTypesInclude;
    }

    public void setDownloadTypesInclude(List<ObjectTypes> downloadTypesInclude) {
        this.downloadTypesInclude = downloadTypesInclude;
    }

    public List<ObjectTypes> getDownloadTypesExclude() {
        return downloadTypesExclude;
    }

    public void setDownloadTypesExclude(List<ObjectTypes> downloadTypesExclude) {
        this.downloadTypesExclude = downloadTypesExclude;
    }

    public Integer getDownloadLimit() {
        return downloadLimit;
    }

    public void setDownloadLimit(Integer downloadLimit) {
        this.downloadLimit = downloadLimit;
    }

    public DocGeneratorOptions getDocGeneratorOptions() {
        return docGeneratorOptions;
    }

    public void setDocGeneratorOptions(DocGeneratorOptions docGeneratorOptions) {
        this.docGeneratorOptions = docGeneratorOptions;
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

    public boolean isIgnoreMissingKeys() {
        return ignoreMissingKeys;
    }

    public void setIgnoreMissingKeys(boolean ignoreMissingKeys) {
        this.ignoreMissingKeys = ignoreMissingKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MidPointConfiguration that = (MidPointConfiguration) o;
        return askToAddMidpointFacet == that.askToAddMidpointFacet
                && askToValidateEnvironmentCredentials == that.askToValidateEnvironmentCredentials
                && ignoreMissingKeys == that.ignoreMissingKeys
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(downloadFilePattern, that.downloadFilePattern)
                && Objects.equals(generatedFilePattern, that.generatedFilePattern)
                && Objects.equals(restClientTimeout, that.restClientTimeout)
                && Objects.equals(restLogCommunication, that.restLogCommunication)
                && Objects.equals(downloadTypesInclude, that.downloadTypesInclude)
                && Objects.equals(downloadTypesExclude, that.downloadTypesExclude)
                && Objects.equals(downloadLimit, that.downloadLimit)
                && Objects.equals(docGeneratorOptions, that.docGeneratorOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                projectId, downloadFilePattern, generatedFilePattern, restClientTimeout, restLogCommunication,
                downloadTypesInclude, downloadTypesExclude, downloadLimit, docGeneratorOptions, askToAddMidpointFacet,
                askToValidateEnvironmentCredentials, ignoreMissingKeys);
    }
}
