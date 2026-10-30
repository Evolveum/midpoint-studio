package com.evolveum.midpoint.studio.impl;

import org.apache.commons.lang3.StringUtils;
import org.linguafranca.pwdb.Entry;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedProperty extends EncryptedObject {

    private String value;

    public EncryptedProperty() {
    }

    public EncryptedProperty(Entry entry) {
        super(entry);

        String password = entry.getProperty(Entry.STANDARD_PROPERTY_NAME_PASSWORD);
        if (StringUtils.isNotEmpty(password)) {
            value = password;
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

    @Override
    public void fillEntry(Entry entry) {
        super.fillEntry(entry);

        setProperty(entry, Entry.STANDARD_PROPERTY_NAME_PASSWORD, value);
    }
}
