package com.evolveum.midpoint.studio.ui.configuration;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExperimentalConfiguration implements Serializable {

    private boolean enableAxiomQueryLanguage;

    private boolean enableMidpointPropertiesLanguage;

    public boolean isEnableAxiomQueryLanguage() {
        return enableAxiomQueryLanguage;
    }

    public void setEnableAxiomQueryLanguage(boolean enableAxiomQueryLanguage) {
        this.enableAxiomQueryLanguage = enableAxiomQueryLanguage;
    }

    public boolean isEnableMidpointPropertiesLanguage() {
        return enableMidpointPropertiesLanguage;
    }

    public void setEnableMidpointPropertiesLanguage(boolean enableMidpointPropertiesLanguage) {
        this.enableMidpointPropertiesLanguage = enableMidpointPropertiesLanguage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentalConfiguration that = (ExperimentalConfiguration) o;

        if (enableAxiomQueryLanguage != that.enableAxiomQueryLanguage) return false;
        return enableMidpointPropertiesLanguage == that.enableMidpointPropertiesLanguage;
    }

    @Override
    public int hashCode() {
        int result = (enableAxiomQueryLanguage ? 1 : 0);
        result = 31 * result + (enableMidpointPropertiesLanguage ? 1 : 0);
        return result;
    }
}
