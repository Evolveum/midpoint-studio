/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.studio.cmd.opts;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
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
    public static final String P_ASK_PASSWORD_LONG = "--password-ask";

    @Parameter(names = {P_URL, P_URL_LONG}, validateWith = URIConverter.class, descriptionKey = "environment.url")
    private String url;

    @Parameter(names = {P_USERNAME, P_USERNAME_LONG}, descriptionKey = "environment.username")
    private String username;

    @Parameter(names = {P_PASSWORD, P_PASSWORD_LONG}, descriptionKey = "environment.password")
    private String password;

    @Parameter(names = {P_ASK_PASSWORD, P_ASK_PASSWORD_LONG}, password = true,
            descriptionKey = "environment.askPassword")
    private String askPassword;

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
}
