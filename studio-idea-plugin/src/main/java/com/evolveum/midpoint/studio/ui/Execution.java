package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.util.Localized;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum Execution implements Localized {

    OID_ONE_BATCH("By OIDs, in one batch"),
    OID_ONE_BY_ONE("By OIDs, one after one"),
    OID_BATCHES_BY_N("By OIDs, in batches of N"),
    ORIGINAL_QUERY("Using original query (selection ignored)");

    private final String key;

    Execution(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
