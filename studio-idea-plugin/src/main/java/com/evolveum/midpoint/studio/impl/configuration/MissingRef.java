package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.studio.util.QNameConverter;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@Tag("reference")
public class MissingRef implements Serializable, Comparable<MissingRef>, Referencable {

    @Attribute
    private String oid;

    @OptionTag(tag = "type", nameAttribute = "", converter = QNameConverter.class)
    private QName type;

    @OptionTag(tag = "name", nameAttribute = "")
    private String name;

    @Attribute
    private MissingRefAction action;

    @Override
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    @Override
    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public MissingRefAction getAction() {
        return action;
    }

    public void setAction(MissingRefAction action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissingRef that = (MissingRef) o;
        return Objects.equals(oid, that.oid)
                && Objects.equals(type, that.type)
                && Objects.equals(name, that.name)
                && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oid, type, name, action);
    }

    @Override
    public int compareTo(@NotNull MissingRef o) {
        return Comparator
                .comparing(
                        MissingRef::getName,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(
                        MissingRef::getType,
                        Comparator.nullsLast(Comparator.comparing(QName::toString)))
                .thenComparing(
                        MissingRef::getOid,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(this, o);

    }

    public MissingRef copy() {
        MissingRef copy = new MissingRef();
        copy.setOid(oid);
        copy.setType(type);
        copy.setName(name);
        copy.setAction(action);

        return copy;
    }

    @Override
    public String toString() {
        return "MissingRef{" +
                "action=" + action +
                ", oid='" + oid + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
