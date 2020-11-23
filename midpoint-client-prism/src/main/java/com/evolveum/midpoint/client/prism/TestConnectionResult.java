package com.evolveum.midpoint.client.prism;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestConnectionResult {

    private boolean success;

    private String version;

    private String revision;

    private Exception exception;

    public TestConnectionResult(boolean success, String version, String revision) {
        this.success = success;
        this.version = version;
        this.revision = revision;
    }

    public TestConnectionResult(boolean success, Exception exception) {
        this.success = success;
        this.exception = exception;
    }

    public boolean success() {
        return this.success;
    }

    public String version() {
        return this.version;
    }

    public String revision() {
        return this.revision;
    }

    public Exception exception() {
        return this.exception;
    }
}
