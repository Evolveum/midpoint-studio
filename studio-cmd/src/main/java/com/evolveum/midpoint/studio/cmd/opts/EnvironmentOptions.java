/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.studio.cmd.opts;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.evolveum.midpoint.studio.client.ProxyType;
import com.evolveum.midpoint.studio.cmd.util.ProxyTypeConverter;
import com.evolveum.midpoint.studio.cmd.util.URIConverter;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages")
public class EnvironmentOptions {

    public static final String P_URL = "-U";
    public static final String P_URL_LONG = "--url";

    public static final String P_USERNAME = "-u";
    public static final String P_USERNAME_LONG = "--username";

    public static final String P_PASSWORD = "-p";
    public static final String P_PASSWORD_LONG = "--password";

    public static final String P_ASK_PASSWORD = "-P";
    public static final String P_ASK_PASSWORD_LONG = "--ask-password";

    public static final String P_PROXY_HOST = "-ph";
    public static final String P_PROXY_HOST_LONG = "--proxy-host";

    public static final String P_PROXY_PORT = "-po";
    public static final String P_PROXY_PORT_LONG = "--proxy-port";

    public static final String P_PROXY_TYPE = "-pt";
    public static final String P_PROXY_TYPE_LONG = "--proxy-type";

    public static final String P_PROXY_USERNAME = "-pu";
    public static final String P_PROXY_USERNAME_LONG = "--proxy-username";

    public static final String P_PROXY_PASSWORD = "-pp";
    public static final String P_PROXY_PASSWORD_LONG = "--proxy-password";

    public static final String P_ASK_PROXY_PASSWORD = "-PP";
    public static final String P_ASK_PROXY_PASSWORD_LONG = "--ask-proxy-password";

    public static final String P_IGNORE_SSL_ERRORS = "-i";
    public static final String P_IGNORE_SSL_ERRORS_LONG = "--ignore-ssl-errors";

    public static final String P_RESPONSE_TIMEOUT = "-t";
    public static final String P_RESPONSE_TIMEOUT_LONG = "--response-timeout";

    @Parameter(names = {P_URL, P_URL_LONG}, validateWith = URIConverter.class, descriptionKey = "environment.url")
    private String url;

    @Parameter(names = {P_USERNAME, P_USERNAME_LONG}, descriptionKey = "environment.username")
    private String username;

    @Parameter(names = {P_PASSWORD, P_PASSWORD_LONG}, descriptionKey = "environment.password")
    private String password;

    @Parameter(names = {P_ASK_PASSWORD, P_ASK_PASSWORD_LONG}, password = true,
            descriptionKey = "environment.askPassword")
    private String askPassword;

    @Parameter(names = {P_PROXY_HOST, P_PROXY_HOST_LONG}, descriptionKey = "environment.proxyHost")
    private String proxyHost;

    @Parameter(names = {P_PROXY_PORT, P_PROXY_PORT_LONG}, descriptionKey = "environment.proxyPort")
    private Integer proxyPort;

    @Parameter(names = {P_PROXY_TYPE, P_PROXY_TYPE_LONG}, descriptionKey = "environment.proxyType", converter = ProxyTypeConverter.class)
    private ProxyType proxyType = ProxyType.HTTP;

    @Parameter(names = {P_PROXY_USERNAME, P_PROXY_USERNAME_LONG}, descriptionKey = "environment.proxyUsername")
    private String proxyUsername;

    @Parameter(names = {P_PROXY_PASSWORD, P_PROXY_PASSWORD_LONG}, descriptionKey = "environment.proxyPassword")
    private String proxyPassword;

    @Parameter(names = {P_ASK_PROXY_PASSWORD, P_ASK_PROXY_PASSWORD_LONG}, password = true,
            descriptionKey = "environment.askProxyPassword")
    private String askProxyPassword;

    @Parameter(names = {P_IGNORE_SSL_ERRORS, P_IGNORE_SSL_ERRORS_LONG}, descriptionKey = "environment.ignoreSSLErrors")
    private boolean ignoreSSLErrors;

    @Parameter(names = {P_RESPONSE_TIMEOUT, P_RESPONSE_TIMEOUT_LONG}, descriptionKey = "environment.responseTimeout")
    private int responseTimeout = 60;

    public String getAskPassword() {
        return askPassword;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getOrAskPassword() {
        String password = getPassword();
        if (password == null) {
            password = getAskPassword();
        }

        return password;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public String getAskProxyPassword() {
        return askProxyPassword;
    }

    public boolean isIgnoreSSLErrors() {
        return ignoreSSLErrors;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public String getOrAskProxyPassword() {
        String password = getProxyPassword();
        if (password == null) {
            password = getAskProxyPassword();
        }

        return password;
    }
}
