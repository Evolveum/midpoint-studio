package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.studio.util.QNameConverter;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag("object")
public class MissingRefObject implements Serializable, Comparable<MissingRefObject> {

    @Attribute
    private String oid;

    @Attribute(converter = QNameConverter.class)
    private QName type;

    @XCollection(style = XCollection.Style.v2)
    private List<MissingRef> references;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public List<MissingRef> getReferences() {
        if (references == null) {
            references = new ArrayList<>();
        }
        return references;
    }

    public void setReferences(List<MissingRef> references) {
        this.references = references;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissingRefObject that = (MissingRefObject) o;
        return Objects.equals(oid, that.oid)
                && Objects.equals(type, that.type)
                && Objects.equals(references, that.references);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oid, type, references);
    }

    @Override
    public int compareTo(@NotNull MissingRefObject o) {
        return Comparator
                .comparing(
                        MissingRefObject::getType,
                        Comparator.nullsLast(Comparator.comparing(QName::toString)))
                .thenComparing(
                        MissingRefObject::getOid,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(this, o);
    }

    public MissingRefObject copy() {
        MissingRefObject copy = new MissingRefObject();
        copy.setOid(oid);
        copy.setType(type);

        List<MissingRef> paths = getReferences().stream()
                .map(MissingRef::copy)
                .collect(Collectors.toList());
        copy.setReferences(paths);

        return copy;
    }

    public boolean isEmpty() {
        return references == null || references.isEmpty();
    }
}
