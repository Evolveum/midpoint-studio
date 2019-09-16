package com.evolveum.midpoint.studio.impl.analytics;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */

public class AnalyticsSettings implements Serializable {

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
