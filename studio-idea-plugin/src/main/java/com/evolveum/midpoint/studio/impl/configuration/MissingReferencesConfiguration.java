package com.evolveum.midpoint.studio.impl.configuration;

import com.intellij.util.xmlb.annotations.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Tag("missingReferences")
public class MissingReferencesConfiguration implements Serializable {

    private List<ObjectReferencesConfiguration> objects;

    private DownloadActionConfiguration action;

    public List<ObjectReferencesConfiguration> getObjects() {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        return objects;
    }

    public void setObjects(List<ObjectReferencesConfiguration> objects) {
        this.objects = objects;
    }

    public DownloadActionConfiguration getAction() {
        return action;
    }

    public void setAction(DownloadActionConfiguration action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissingReferencesConfiguration that = (MissingReferencesConfiguration) o;
        return Objects.equals(objects, that.objects)
                && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objects, action);
    }

    public MissingReferencesConfiguration copy() {
        MissingReferencesConfiguration copy = new MissingReferencesConfiguration();
        if (objects != null) {
            copy.setObjects(objects.stream().map(ObjectReferencesConfiguration::copy).toList());
        }
        copy.setAction(action);
        return copy;
    }
}
