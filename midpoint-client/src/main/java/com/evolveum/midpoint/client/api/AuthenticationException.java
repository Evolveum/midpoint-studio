
package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.util.exception.SecurityViolationException;

public class AuthenticationException extends SecurityViolationException {

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
