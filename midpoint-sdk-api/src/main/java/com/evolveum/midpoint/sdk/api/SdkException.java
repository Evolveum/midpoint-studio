package com.evolveum.midpoint.sdk.api;

public class SdkException extends Exception {

    public SdkException(String message) {
        super(message);
    }

    public SdkException(String message, Throwable cause) {
        super(message, cause);
    }
}
