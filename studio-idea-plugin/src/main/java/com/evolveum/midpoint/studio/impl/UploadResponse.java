package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.result.OperationResult;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadResponse {

    private String oid;

    private OperationResult result;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public OperationResult getResult() {
        return result;
    }

    public void setResult(OperationResult result) {
        this.result = result;
    }
}
