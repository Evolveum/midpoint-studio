package com.evolveum.midpoint.studio.impl;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */

public class AnalyticsSettings implements Serializable {

    /**
     * used to decide whether to show "send usage statistics"
     */
    private String pluginVersion;
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }
}
