package com.evolveum.midpoint.client.prism;

import java.net.Proxy;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestServiceConfiguration {

    private String url;

    private String username;

    private String password;

    private boolean ignoreSSLErrors;

    private String proxyServer;

    private Integer proxyServerPort;

    private Proxy.Type proxyServerType = Proxy.Type.HTTP;

    private String proxyUsername;

    private String proxyPassword;

    private MessageListener messageListener;

    public String url() {
        return this.url;
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

    public boolean ignoreSSLErrors() {
        return this.ignoreSSLErrors;
    }

    public String proxyServer() {
        return this.proxyServer;
    }

    public Integer proxyServerPort() {
        return this.proxyServerPort;
    }

    public Proxy.Type proxyServerType() {
        return this.proxyServerType;
    }

    public String proxyUsername() {
        return this.proxyUsername;
    }

    public String proxyPassword() {
        return this.proxyPassword;
    }

    public MessageListener messageListener() {
        return this.messageListener;
    }

    public RestServiceConfiguration url(final String url) {
        this.url = url;
        return this;
    }

    public RestServiceConfiguration username(final String username) {
        this.username = username;
        return this;
    }

    public RestServiceConfiguration password(final String password) {
        this.password = password;
        return this;
    }

    public RestServiceConfiguration ignoreSSLErrors(final boolean ignoreSSLErrors) {
        this.ignoreSSLErrors = ignoreSSLErrors;
        return this;
    }

    public RestServiceConfiguration proxyServer(final String proxyServer) {
        this.proxyServer = proxyServer;
        return this;
    }

    public RestServiceConfiguration proxyServerPort(final Integer proxyServerPort) {
        this.proxyServerPort = proxyServerPort;
        return this;
    }

    public RestServiceConfiguration proxyServerType(final Proxy.Type proxyServerType) {
        this.proxyServerType = proxyServerType;
        return this;
    }

    public RestServiceConfiguration proxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
        return this;
    }

    public RestServiceConfiguration proxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
        return this;
    }

    public RestServiceConfiguration messageListener(final MessageListener messageListener) {
        this.messageListener = messageListener;
        return this;
    }
}
