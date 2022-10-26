package com.evolveum.midpoint.studio.gradle;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidpointStudioPluginExtension {

    private String midpointVersion = Constants.MIDPOINT_LATEST_VERSION;

    private boolean checkPluginUpdates = true;

    public String getMidpointVersion() {
        return midpointVersion;
    }

    public void setMidpointVersion(String midpointVersion) {
        this.midpointVersion = midpointVersion;
    }

    public boolean isCheckPluginUpdates() {
        return checkPluginUpdates;
    }

    public void setCheckPluginUpdates(boolean checkPluginUpdates) {
        this.checkPluginUpdates = checkPluginUpdates;
    }
}
