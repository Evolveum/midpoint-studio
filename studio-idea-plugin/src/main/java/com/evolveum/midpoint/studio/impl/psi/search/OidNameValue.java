package com.evolveum.midpoint.studio.impl.psi.search;

import com.evolveum.midpoint.schema.constants.ObjectTypes;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidNameValue {

    private String oid;

    private String name;

    private ObjectTypes type;

    public OidNameValue(String oid, String name, ObjectTypes type) {
        this.oid = oid;
        this.name = name != null ? name : "";
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OidNameValue that = (OidNameValue) o;

        if (oid != null ? !oid.equals(that.oid) : that.oid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = oid != null ? oid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OidNameValue{" +
                "oid='" + oid + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
