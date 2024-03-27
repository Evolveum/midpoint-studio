package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.studio.util.QNameConverter;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Tag("object")
public class ObjectReferencesConfiguration implements Serializable, Comparable<ObjectReferencesConfiguration> {

    private String oid;

    @OptionTag(tag = "type", nameAttribute = "", converter = QNameConverter.class)
    private QName type;

    private List<ReferenceConfiguration> ignoredReferences;

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

    public List<ReferenceConfiguration> getIgnoredReferences() {
        if (ignoredReferences == null) {
            ignoredReferences = new ArrayList<>();
        }
        return ignoredReferences;
    }

    public void setIgnoredReferences(List<ReferenceConfiguration> ignoredReferences) {
        this.ignoredReferences = ignoredReferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectReferencesConfiguration that = (ObjectReferencesConfiguration) o;
        return Objects.equals(oid, that.oid)
                && Objects.equals(type, that.type)
                && Objects.equals(ignoredReferences, that.ignoredReferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oid, type, ignoredReferences);
    }

    @Override
    public int compareTo(@NotNull ObjectReferencesConfiguration o) {
        return Comparator
                .comparing(
                        ObjectReferencesConfiguration::getType,
                        Comparator.nullsLast(Comparator.comparing(QName::toString)))
                .thenComparing(
                        ObjectReferencesConfiguration::getOid,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(this, o);
    }

    public ObjectReferencesConfiguration copy() {
        ObjectReferencesConfiguration copy = new ObjectReferencesConfiguration();
        copy.setOid(oid);
        copy.setType(type);

        List<ReferenceConfiguration> paths = getIgnoredReferences().stream()
                .map(ReferenceConfiguration::copy)
                .toList();
        copy.setIgnoredReferences(paths);

        return copy;
    }
}
