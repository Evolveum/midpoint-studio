package com.evolveum.midpoint.sdk.api.client;

public class MidpointClientConfiguration {

    private String url;

    private String username;

    private String password;

    private boolean ignoreSSLErrors;

    private String proxyServer;

    private String proxyUsername;

    private String proxyPassword;

    private MessageListener messageListener;

    private int responseTimeout = 60;

    private boolean useHttp2 = false;

    public String url() {
        return url;
    }

    public MidpointClientConfiguration url(String url) {
        this.url = url;
        return this;
    }

    public String username() {
        return username;
    }

    public MidpointClientConfiguration username(String username) {
        this.username = username;
        return this;
    }

    public String password() {
        return password;
    }

    public MidpointClientConfiguration password(String password) {
        this.password = password;
        return this;
    }

    public boolean ignoreSSLErrors() {
        return ignoreSSLErrors;
    }

    public MidpointClientConfiguration ignoreSSLErrors(boolean ignoreSSLErrors) {
        this.ignoreSSLErrors = ignoreSSLErrors;
        return this;
    }

    public String proxyServer() {
        return proxyServer;
    }

    public MidpointClientConfiguration proxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
        return this;
    }

    public String proxyUsername() {
        return proxyUsername;
    }

    public MidpointClientConfiguration proxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
        return this;
    }

    public String proxyPassword() {
        return proxyPassword;
    }

    public MidpointClientConfiguration proxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
        return this;
    }

    public MessageListener messageListener() {
        return messageListener;
    }

    public MidpointClientConfiguration messageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
        return this;
    }

    public int responseTimeout() {
        return responseTimeout;
    }

    public MidpointClientConfiguration responseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    public boolean useHttp2() {
        return useHttp2;
    }

    public MidpointClientConfiguration useHttp2(boolean useHttp2) {
        this.useHttp2 = useHttp2;
        return this;
    }
}
