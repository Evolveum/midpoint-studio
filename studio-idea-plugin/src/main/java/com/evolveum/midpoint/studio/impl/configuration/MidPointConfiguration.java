package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.impl.DocGeneratorOptions;
import com.evolveum.midpoint.studio.impl.cache.Cache;
import com.evolveum.midpoint.studio.util.ObjectTypesListConverter;
import com.intellij.util.xmlb.annotations.OptionTag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointConfiguration implements Serializable {

    private static final List<ObjectTypes> DEFAULT_DOWNLOAD_EXCLUDE;

    static {
        DEFAULT_DOWNLOAD_EXCLUDE = List.of(ObjectTypes.USER,
                ObjectTypes.SHADOW,
                ObjectTypes.CASE,
                ObjectTypes.REPORT_DATA,
                ObjectTypes.CONNECTOR,
                ObjectTypes.ACCESS_CERTIFICATION_CAMPAIGN,
                ObjectTypes.NODE);
    }

    private String projectId;

    @Deprecated
    private String dowloadFilePattern;

    private String downloadFilePattern;

    private String generatedFilePattern;

    private boolean printRestCommunicationToConsole;

    private DocGeneratorOptions docGeneratorOptions;

    private boolean askToAddMidpointFacet;

    private boolean askToValidateEnvironmentCredentials;

    private boolean ignoreMissingKeys;

    @OptionTag(converter = ObjectTypesListConverter.class)
    private List<ObjectTypes> downloadTypesInclude;

    @OptionTag(converter = ObjectTypesListConverter.class)
    private List<ObjectTypes> downloadTypesExclude;

    private int typesToDownloadLimit;

    private int restResponseTimeout;

    private String midpointVersion;

    private boolean updateOnUpload;

    private int cacheTTL = Cache.DEFAULT_CACHE_TTL;

    public MidPointConfiguration() {
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

    public String getDownloadFilePattern() {
        return downloadFilePattern;
    }

    public void setDownloadFilePattern(String downloadFilePattern) {
        this.downloadFilePattern = downloadFilePattern;
    }

    public String getMidpointVersion() {
        return midpointVersion;
    }

    public void setMidpointVersion(String midpointVersion) {
        this.midpointVersion = midpointVersion;
    }

    public boolean isUpdateOnUpload() {
        return updateOnUpload;
    }

    public void setUpdateOnUpload(boolean updateOnUpload) {
        this.updateOnUpload = updateOnUpload;
    }

    public int getCacheTTL() {
        return cacheTTL;
    }

    public void setCacheTTL(int cacheTTL) {
        this.cacheTTL = cacheTTL;
    }

    public MidPointConfiguration copy() {
        MidPointConfiguration other = new MidPointConfiguration();
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
        other.downloadFilePattern = downloadFilePattern;
        other.midpointVersion = midpointVersion;
        other.updateOnUpload = updateOnUpload;
        other.cacheTTL = cacheTTL;
        // todo copy doc generator options

        return other;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MidPointConfiguration that = (MidPointConfiguration) o;
        return printRestCommunicationToConsole == that.printRestCommunicationToConsole
                && askToAddMidpointFacet == that.askToAddMidpointFacet
                && askToValidateEnvironmentCredentials == that.askToValidateEnvironmentCredentials
                && ignoreMissingKeys == that.ignoreMissingKeys
                && typesToDownloadLimit == that.typesToDownloadLimit
                && restResponseTimeout == that.restResponseTimeout
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(dowloadFilePattern, that.dowloadFilePattern)
                && Objects.equals(generatedFilePattern, that.generatedFilePattern)
                && Objects.equals(docGeneratorOptions, that.docGeneratorOptions)
                && Objects.equals(downloadTypesInclude, that.downloadTypesInclude)
                && Objects.equals(downloadTypesExclude, that.downloadTypesExclude)
                && Objects.equals(downloadFilePattern, that.downloadFilePattern)
                && Objects.equals(midpointVersion, that.midpointVersion)
                && Objects.equals(updateOnUpload, that.updateOnUpload)
                && Objects.equals(cacheTTL, that.cacheTTL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                projectId, dowloadFilePattern, generatedFilePattern, printRestCommunicationToConsole, docGeneratorOptions,
                askToAddMidpointFacet, askToValidateEnvironmentCredentials, ignoreMissingKeys, downloadTypesInclude,
                downloadTypesExclude, typesToDownloadLimit, restResponseTimeout, downloadFilePattern, midpointVersion,
                updateOnUpload, cacheTTL);
    }

    @Override
    public String toString() {
        return "MidPointSettings{projectId='" + projectId + "'}";
    }

    public static MidPointConfiguration createDefaultSettings() {
        MidPointConfiguration settings = new MidPointConfiguration();
        settings.setProjectId(UUID.randomUUID().toString());

        settings.setDowloadFilePattern("objects/$T/$n.xml");
        settings.setGeneratedFilePattern("scratches/gen/$n.xml");

        settings.setDownloadTypesExclude(new ArrayList<>(DEFAULT_DOWNLOAD_EXCLUDE));
        settings.setTypesToDownloadLimit(100);
        settings.updateOnUpload = false;
        settings.setMidpointVersion(MidPointConstants.DEFAULT_MIDPOINT_VERSION);

        settings.askToAddMidpointFacet = true;
        settings.askToValidateEnvironmentCredentials = true;
        settings.restResponseTimeout = 60;
        settings.cacheTTL = Cache.DEFAULT_CACHE_TTL;

        return settings;
    }
}
