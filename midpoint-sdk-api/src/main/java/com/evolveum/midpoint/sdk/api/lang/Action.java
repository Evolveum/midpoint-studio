package com.evolveum.midpoint.sdk.api.lang;

import javax.xml.namespace.QName;
import java.util.Comparator;
import java.util.Objects;

public class Action implements Comparable<Action> {

    private QName name;

    private String source;

    public Action(QName name, String source) {
        this.name = name;
        this.source = source;
    }

    public QName name() {
        return name;
    }

    public String source() {
        return source;
    }

    @Override
    public int compareTo(Action o) {
        String q1 = name != null ? name.getLocalPart() : null;
        String q2 = o != null && o.name != null ? o.name.getLocalPart() : null;

        return Comparator.<String>naturalOrder().compare(q1, q2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return Objects.equals(name, action.name) && Objects.equals(source, action.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source);
    }
}
