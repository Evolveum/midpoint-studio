package com.evolveum.midpoint.studio.ui.smart.suggestion.component.table;

public class SmartSuggestionObject<T> {

    T object;
    String resourceOid;

    public SmartSuggestionObject(T object, String resourceOid) {
        this.object = object;
        this.resourceOid = resourceOid;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public String getResourceOid() {
        return resourceOid;
    }
}
