package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;

import java.util.Collection;

public class GeneratorOptions {

    private boolean symbolicReferences;
    private boolean symbolicReferencesRuntime;
    private boolean createSuspended;
    private boolean raw;
    private boolean dryRun;
    private boolean useActivities;

    private boolean batchByOids;
    private boolean batchUsingOriginalQuery;
    private int batchSize = 100;
    private String originalQuery;
    private Collection<ObjectTypes> originalQueryTypes;

    public boolean isSymbolicReferences() {
        return symbolicReferences;
    }

    public void setSymbolicReferences(boolean symbolicReferences) {
        this.symbolicReferences = symbolicReferences;
    }

    public boolean isSymbolicReferencesRuntime() {
        return symbolicReferencesRuntime;
    }

    public void setSymbolicReferencesRuntime(boolean symbolicReferencesRuntime) {
        this.symbolicReferencesRuntime = symbolicReferencesRuntime;
    }

    public boolean isCreateSuspended() {
        return createSuspended;
    }

    public void setCreateSuspended(boolean createSuspended) {
        this.createSuspended = createSuspended;
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isBatchByOids() {
        return batchByOids;
    }

    public void setBatchByOids(boolean batchByOids) {
        this.batchByOids = batchByOids;
    }

    public boolean isBatchUsingOriginalQuery() {
        return batchUsingOriginalQuery;
    }

    public void setBatchUsingOriginalQuery(boolean batchUsingOriginalQuery) {
        this.batchUsingOriginalQuery = batchUsingOriginalQuery;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }

    public Collection<ObjectTypes> getOriginalQueryTypes() {
        return originalQueryTypes;
    }

    public void setOriginalQueryTypes(Collection<ObjectTypes> originalQueryTypes) {
        this.originalQueryTypes = originalQueryTypes;
    }

    public boolean isUseActivities() {
        return useActivities;
    }

    public void setUseActivities(boolean useActivities) {
        this.useActivities = useActivities;
    }
}

