package com.evolveum.midpoint.studio.impl;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedCredentials extends EncryptedProperty {

    private String username;

    public EncryptedCredentials() {
    }

    public EncryptedCredentials(Entry entry) {
        super(entry);

        if (StringUtils.isNotEmpty(entry.getUsername())) {
            this.username = entry.getUsername();
        }
    }

    public EncryptedCredentials(String key, String environment, String username, String password, String description) {
        super(key, environment, password, description);

        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return getValue();
    }

    public void setPassword(String password) {
        setValue(password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EncryptedCredentials that = (EncryptedCredentials) o;

        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }

    @Override
    public EntryBuilder buildEntry() {
        EntryBuilder builder = super.buildEntry();
        builder.username(username);

        return builder;
    }
}
