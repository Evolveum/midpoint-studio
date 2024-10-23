package com.evolveum.midpoint.studio.impl.xml;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum LocationType {

    LOCAL("local"),

    REMOTE("remote");

    private final String value;

    LocationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LocationType fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (LocationType l : values()) {
            if (l.getValue().equals(value)) {
                return l;
            }
        }

        return null;
    }
}
