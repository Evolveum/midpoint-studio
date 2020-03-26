package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.ArrayList;
import java.util.List;

public class Batch {

    private List<ObjectType> objects = new ArrayList<>();

    private int first;

    public List<ObjectType> getObjects() {
        return objects;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getFirst() {
        return first;
    }

    public int getLast() {
        return first + objects.size() - 1;
    }

}
