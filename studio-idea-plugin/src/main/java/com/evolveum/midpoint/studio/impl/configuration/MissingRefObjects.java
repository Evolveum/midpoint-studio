package com.evolveum.midpoint.studio.impl.configuration;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag("missingRefObjects")
public class MissingRefObjects implements Serializable {

    private List<MissingRefObject> objects;

    @OptionTag(nameAttribute = "")
    private MissingRefAction defaultAction;

    @XCollection(style = XCollection.Style.v2)
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
            copy.setObjects(objects.stream().map(MissingRefObject::copy).collect(Collectors.toList()));
        }
        copy.setDefaultAction(defaultAction);
        return copy;
    }
}
