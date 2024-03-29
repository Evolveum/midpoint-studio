package com.evolveum.midpoint.studio.impl.configuration;

import com.intellij.util.xmlb.annotations.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Tag("objects")
public class MissingRefObjects implements Serializable {

    private List<MissingRefObject> objects;

    private MissingRefAction defaultAction;

    public List<MissingRefObject> getObjects() {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        return objects;
    }

    public void setObjects(List<MissingRefObject> objects) {
        this.objects = objects;
    }

    public MissingRefAction getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(MissingRefAction defaultAction) {
        this.defaultAction = defaultAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissingRefObjects that = (MissingRefObjects) o;
        return Objects.equals(objects, that.objects)
                && defaultAction == that.defaultAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objects, defaultAction);
    }

    public MissingRefObjects copy() {
        MissingRefObjects copy = new MissingRefObjects();
        if (objects != null) {
            copy.setObjects(objects.stream().map(MissingRefObject::copy).toList());
        }
        copy.setDefaultAction(defaultAction);
        return copy;
    }
}
