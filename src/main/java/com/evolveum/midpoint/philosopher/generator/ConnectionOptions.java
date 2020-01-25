package com.evolveum.midpoint.philosopher.generator;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.evolveum.midpoint.philosopher.util.URIConverter;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages")
public class ConnectionOptions {

    public static final String P_URL = "-U";
    public static final String P_URL_LONG = "--url";

    public static final String P_USERNAME = "-u";
    public static final String P_USERNAME_LONG = "--username";

    public static final String P_PASSWORD = "-p";
    public static final String P_PASSWORD_LONG = "--password";

    public static final String P_ASK_PASSWORD = "-P";
    public static final String P_ASK_PASSWORD_LONG = "--password-ask";

    @Parameter(names = {P_URL, P_URL_LONG}, validateWith = URIConverter.class, descriptionKey = "connection.url")
    private String url;

    @Parameter(names = {P_USERNAME, P_USERNAME_LONG}, descriptionKey = "connection.username")
    private String username;

    @Parameter(names = {P_PASSWORD, P_PASSWORD_LONG}, descriptionKey = "connection.password")
    private String password;

    @Parameter(names = {P_ASK_PASSWORD, P_ASK_PASSWORD_LONG}, password = true,
            descriptionKey = "connection.askPassword")
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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAskPassword(String askPassword) {
        this.askPassword = askPassword;
    }
}
