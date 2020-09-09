package com.evolveum.midpoint.studio.impl.psi.search;

import com.evolveum.midpoint.schema.constants.ObjectTypes;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidNameValue {

    private String oid;

    private String name;

    private ObjectTypes type;

    private String source;

    public OidNameValue(String oid, String name, ObjectTypes type, String source) {
        this.oid = oid;
        this.name = name != null ? name : "";
        this.type = type;
        this.source = source;
    }

    public String getOid() {
        return oid;
    }

    public String getName() {
        return name;
    }

    public ObjectTypes getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OidNameValue that = (OidNameValue) o;

        if (oid != null ? !oid.equals(that.oid) : that.oid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != that.type) return false;
        return source != null ? source.equals(that.source) : that.source == null;
    }

    @Override
    public int hashCode() {
        int result = oid != null ? oid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OidNameValue{" +
                "oid='" + oid + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", source='" + source + '\'' +
                '}';
    }
}
