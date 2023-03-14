package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.intellij.openapi.components.BaseState;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointSettingsState implements Serializable {

    private static final List<ObjectTypes> DOWNLOAD_EXLUDE;

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

        DOWNLOAD_EXLUDE = Collections.unmodifiableList(types);
    }

    String downloadFilePattern = "objects/$T/$n.xml";

    String generatedFilePattern = "scratches/gen/$n.xml";

    Integer restClientTimeout = 60;

    Boolean restLogCommunication = false;

    List<ObjectTypes> downloadTypesInclude = new ArrayList<>();

    List<ObjectTypes> downloadTypesExclude = new ArrayList<>(DOWNLOAD_EXLUDE);

    Integer downloadLimit = 100;

    Boolean askToAddMidpointFacet = true;

    Boolean askToValidateEnvironmentCredentials = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MidPointSettingsState that = (MidPointSettingsState) o;

        if (!Objects.equals(downloadFilePattern, that.downloadFilePattern))
            return false;
        if (!Objects.equals(generatedFilePattern, that.generatedFilePattern))
            return false;
        if (!Objects.equals(restClientTimeout, that.restClientTimeout))
            return false;
        if (!Objects.equals(restLogCommunication, that.restLogCommunication))
            return false;
        if (!Objects.equals(downloadTypesInclude, that.downloadTypesInclude))
            return false;
        if (!Objects.equals(downloadTypesExclude, that.downloadTypesExclude))
            return false;
        if (!Objects.equals(downloadLimit, that.downloadLimit))
            return false;
        if (!Objects.equals(askToAddMidpointFacet, that.askToAddMidpointFacet))
            return false;
        return Objects.equals(askToValidateEnvironmentCredentials, that.askToValidateEnvironmentCredentials);
    }

    @Override
    public int hashCode() {
        int result = downloadFilePattern != null ? downloadFilePattern.hashCode() : 0;
        result = 31 * result + (generatedFilePattern != null ? generatedFilePattern.hashCode() : 0);
        result = 31 * result + (restClientTimeout != null ? restClientTimeout.hashCode() : 0);
        result = 31 * result + (restLogCommunication != null ? restLogCommunication.hashCode() : 0);
        result = 31 * result + (downloadTypesInclude != null ? downloadTypesInclude.hashCode() : 0);
        result = 31 * result + (downloadTypesExclude != null ? downloadTypesExclude.hashCode() : 0);
        result = 31 * result + (downloadLimit != null ? downloadLimit.hashCode() : 0);
        result = 31 * result + (askToAddMidpointFacet != null ? askToAddMidpointFacet.hashCode() : 0);
        result = 31 * result + (askToValidateEnvironmentCredentials != null ? askToValidateEnvironmentCredentials.hashCode() : 0);
        return result;
    }
}
