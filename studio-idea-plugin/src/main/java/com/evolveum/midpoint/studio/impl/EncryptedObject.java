package com.evolveum.midpoint.studio.impl;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class EncryptedObject implements Serializable {

    private String key;

    private String environment;

    private String description;

    public EncryptedObject() {
    }

    public EncryptedObject(Entry entry) {
        if (StringUtils.isNotEmpty(entry.getTitle())) {
            key = entry.getTitle();
        }
        if (StringUtils.isNotEmpty(entry.getUrl())) {
            environment = entry.getUrl();
        }
        if (StringUtils.isNotEmpty(entry.getNotes())) {
            description = entry.getNotes();
        }
    }

    public EncryptedObject(String key, String environment, String description) {
        this.key = key;
        this.environment = environment;
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

        EncryptedObject that = (EncryptedObject) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (environment != null ? !environment.equals(that.environment) : that.environment != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (environment != null ? environment.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    public EntryBuilder buildEntry() {
        return new EntryBuilder()
                .title(key)
                .url(environment)
                .notes(description)
                .tags(Arrays.asList(getClass().getName()));
    }
}
