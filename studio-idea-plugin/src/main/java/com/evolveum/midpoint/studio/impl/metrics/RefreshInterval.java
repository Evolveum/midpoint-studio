package com.evolveum.midpoint.studio.impl.metrics;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum RefreshInterval {

    SECONDS_5("5s", 5),

    SECONDS_15("15s", 15),

    SECONDS_30("30s", 30),

    MINUTE_1("1m", 60),

    MINUTE_5("5m", 300);

    private String label;

    private int seconds;

    RefreshInterval(String label, int seconds) {
        this.label = label;
        this.seconds = seconds;
    }

    public String getLabel() {
        return label;
    }

    public int getSeconds() {
        return seconds;
    }
}
