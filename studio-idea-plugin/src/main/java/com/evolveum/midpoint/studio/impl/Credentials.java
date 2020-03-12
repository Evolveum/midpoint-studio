package com.evolveum.midpoint.studio.impl;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Credentials implements Serializable {

    private String key;
    private String environment;

    private String username;
    private String password;

    private String description;

    public Credentials() {
    }

    public Credentials(String key, String environment, String username, String password, String description) {
        this.key = key;
        this.environment = environment;
        this.username = username;
        this.password = password;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credentials that = (Credentials) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (environment != null ? !environment.equals(that.environment) : that.environment != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (environment != null ? environment.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "key='" + key + '\'' +
                ", env='" + environment + '\'' +
                ", username='" + username + '\'' +
                ", password (" + (password != null ? password.length() : null) +
                '}';
    }
}
