package com.evolveum.midpoint.studio.impl;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 *
 * todo review and delete if necessary
 */
@Deprecated
public class HintEntry implements Serializable {

    private String oid;
    private QName type;
    private String name;

    public HintEntry(String oid, QName type, String name) {
        this.oid = oid;
        this.type = type;
        this.name = name;
    }

    public String getOid() {
        return oid;
    }

    public QName getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HintEntry hintEntry = (HintEntry) o;

        if (!Objects.equals(oid, hintEntry.oid)) return false;
        if (!Objects.equals(type, hintEntry.type)) return false;
        return Objects.equals(name, hintEntry.name);
    }

    @Override
    public int hashCode() {
        int result = oid != null ? oid.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HintEntry{" +
                "oid='" + oid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
