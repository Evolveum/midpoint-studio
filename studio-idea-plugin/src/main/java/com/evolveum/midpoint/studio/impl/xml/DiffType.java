package com.evolveum.midpoint.studio.impl.xml;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DiffType {

    private DiffObjectType firstObject;

    private DiffObjectType secondObject;

    public DiffObjectType getFirstObject() {
        return firstObject;
    }

    public void setFirstObject(DiffObjectType firstObject) {
        this.firstObject = firstObject;
    }

    public DiffObjectType getSecondObject() {
        return secondObject;
    }

    public void setSecondObject(DiffObjectType secondObject) {
        this.secondObject = secondObject;
    }
}
