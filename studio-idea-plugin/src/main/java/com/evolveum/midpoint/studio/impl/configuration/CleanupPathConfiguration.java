package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.util.cleanup.CleanupPath;
import com.evolveum.midpoint.studio.util.ItemPathConverter;
import com.evolveum.midpoint.studio.util.QNameConverter;
import com.evolveum.midpoint.util.QNameUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class CleanupPathConfiguration implements Serializable, Comparable<CleanupPathConfiguration> {

    @OptionTag(converter = QNameConverter.class)
    private QName type;

    @OptionTag(converter = ItemPathConverter.class)
    private ItemPath path;

    private CleanupPathActionConfiguration action;

    @SuppressWarnings("unused")
    public CleanupPathConfiguration() {
    }

    public CleanupPathConfiguration(QName type, ItemPath path, CleanupPathActionConfiguration action) {
        this.type = type;
        this.path = path;
        this.action = action;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public ItemPath getPath() {
        return path;
    }

    public void setPath(ItemPath path) {
        this.path = path;
    }

    public CleanupPathActionConfiguration getAction() {
        return action;
    }

    public void setAction(CleanupPathActionConfiguration action) {
        this.action = action;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CleanupPathConfiguration that = (CleanupPathConfiguration) o;
        return type == that.type && Objects.equals(path, that.path) && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path, action);
    }

    private String getTypeAsString() {
        return type != null ? QNameUtil.qNameToUri(type) : null;
    }

    private String getPathAsString() {
        return path != null ? path.toString() : null;
    }

    @Override
    public int compareTo(@NotNull CleanupPathConfiguration o) {
        return Comparator.nullsLast(Comparator.comparing(CleanupPathConfiguration::getTypeAsString))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(CleanupPathConfiguration::getPathAsString)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(CleanupPathConfiguration::getAction)))
                .compare(this, o);
    }

    public CleanupPathConfiguration copy() {
        return new CleanupPathConfiguration(type, path, action);
    }

    public CleanupPath toCleanupPath() {
        return new CleanupPath(type, path, action != null ? action.value() : null);
    }
}
