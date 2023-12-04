package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.CleanupPathActionConverter;
import com.evolveum.midpoint.studio.util.ObjectTypesConverter;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class  CleanupPath implements Serializable, Comparable<CleanupPath> {

    @OptionTag(converter = ObjectTypesConverter.class)
    private ObjectTypes type;

    private String path;


    @OptionTag(converter = CleanupPathActionConverter.class)
    private CleanupPathAction action;

    @SuppressWarnings("unused")
    public CleanupPath() {
    }

    public CleanupPath(ObjectTypes type, String path, CleanupPathAction action) {
        this.type = type;
        this.path = path;
        this.action = action;
    }

    public ObjectTypes getType() {
        return type;
    }

    public void setType(ObjectTypes type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CleanupPathAction getAction() {
        return action;
    }

    public void setAction(CleanupPathAction action) {
        this.action = action;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CleanupPath that = (CleanupPath) o;
        return type == that.type && Objects.equals(path, that.path) && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path, action);
    }

    @Override
    public int compareTo(@NotNull CleanupPath o) {
        return Comparator.nullsLast(Comparator.comparing(CleanupPath::getType))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(CleanupPath::getPath)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(CleanupPath::getAction)))
                .compare(this, o);
    }

    public CleanupPath copy() {
        return new CleanupPath(type, path, action);
    }
}
