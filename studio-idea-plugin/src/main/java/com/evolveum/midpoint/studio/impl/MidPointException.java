package com.evolveum.midpoint.studio.impl;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointException extends RuntimeException {

    public MidPointException(String message) {
        super(message);
    }

    public MidPointException(String message, Throwable cause) {
        super(message, cause);
    }
}
