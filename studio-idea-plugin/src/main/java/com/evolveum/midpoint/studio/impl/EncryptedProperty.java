package com.evolveum.midpoint.studio.impl;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedProperty extends EncryptedObject {

    private String value;

    public EncryptedProperty() {
    }

    public EncryptedProperty(Entry entry) {
        super(entry);

        if (StringUtils.isNotEmpty(entry.getPassword())) {
            value = entry.getPassword();
        }
    }

    public EncryptedProperty(String key, String environment, String value, String description) {
        super(key, environment, description);

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EncryptedProperty property = (EncryptedProperty) o;

        return value != null ? value.equals(property.value) : property.value == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public EntryBuilder buildEntry() {
        EntryBuilder builder = super.buildEntry();
        builder.password(value);

        return builder;
    }
}
