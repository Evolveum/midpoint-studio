package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.schema.result.OperationResult;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ClientException extends RuntimeException {

    private OperationResult result;

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(String message, OperationResult result) {
        super(message);
        this.result = result;
    }

    public OperationResult getResult() {
        return result;
    }
}
