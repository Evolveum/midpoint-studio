package com.evolveum.midpoint.studio.gradle;

import org.gradle.api.Project;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum MidpointStudioPluginFeature {

    SELF_UPDATE_CHECK("selfUpdateCheck", true);

    private String featureName;

    private boolean defaultValue;

    MidpointStudioPluginFeature(String featureName, boolean defaultValue) {
        this.featureName = featureName;
        this.defaultValue = defaultValue;
    }

    public String getFeatureName() {
        return featureName;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return getClass().getPackageName() + getFeatureName();
    }

    public boolean isPluginFeatureEnabled(Project project) {
        return isPluginFeatureEnabled(project, this);
    }

    public static boolean isPluginFeatureEnabled(Project project, MidpointStudioPluginFeature feature) {
        Object value = project.findProperty(feature.toString());
        if (value == null || !(value instanceof Boolean)) {
            return feature.getDefaultValue();
        }

        return (boolean) value;
    }
}
