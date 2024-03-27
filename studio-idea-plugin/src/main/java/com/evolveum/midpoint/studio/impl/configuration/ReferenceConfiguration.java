package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.studio.util.QNameConverter;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@Tag("reference")
public class ReferenceConfiguration implements Serializable, Comparable<ReferenceConfiguration> {

    @Attribute
    private String oid;

    @Attribute(converter = QNameConverter.class)
    private QName type;

    @Attribute
    private ReferenceDecisionConfiguration decision;

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

    public ReferenceDecisionConfiguration getDecision() {
        return decision;
    }

    public void setDecision(ReferenceDecisionConfiguration decision) {
        this.decision = decision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferenceConfiguration that = (ReferenceConfiguration) o;
        return Objects.equals(oid, that.oid)
                && Objects.equals(type, that.type)
                && decision == that.decision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oid, type, decision);
    }

    @Override
    public int compareTo(@NotNull ReferenceConfiguration o) {
        return Comparator
                .comparing(ReferenceConfiguration::getType, Comparator.nullsLast(Comparator.comparing(QName::toString)))
                .thenComparing(ReferenceConfiguration::getOid, Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(this, o);

    }

    public ReferenceConfiguration copy() {
        ReferenceConfiguration copy = new ReferenceConfiguration();
        copy.setOid(oid);
        copy.setType(type);
        copy.setDecision(decision);

        return copy;
    }
}
