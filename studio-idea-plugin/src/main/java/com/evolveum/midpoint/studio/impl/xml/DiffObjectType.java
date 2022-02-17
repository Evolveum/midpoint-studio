package com.evolveum.midpoint.studio.impl.xml;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DiffObjectType {

    private String fileName;

    private LocationType location;

    private ObjectType object;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocationType getLocation() {
        return location;
    }

    public void setLocation(LocationType location) {
        this.location = location;
    }

    public ObjectType getObject() {
        return object;
    }

    public void setObject(ObjectType object) {
        this.object = object;
    }
}
