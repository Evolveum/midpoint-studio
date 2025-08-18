package com.evolveum.midpoint.studio.util.exception;

public class UnsupportedLanguageException extends RuntimeException {
    public UnsupportedLanguageException(String language) {
        super("Unsupported language: " + language);
    }
}
