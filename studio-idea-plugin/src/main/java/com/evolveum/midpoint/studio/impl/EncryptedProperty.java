package com.evolveum.midpoint.studio.impl;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedProperty implements Serializable {

    private String key;

    private String environment;

    private String value;

    private String description;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

        EncryptedProperty that = (EncryptedProperty) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (environment != null ? !environment.equals(that.environment) : that.environment != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (environment != null ? environment.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
