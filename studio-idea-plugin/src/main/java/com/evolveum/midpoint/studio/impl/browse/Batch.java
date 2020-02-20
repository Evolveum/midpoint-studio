package com.evolveum.midpoint.studio.impl.browse;

import java.util.ArrayList;
import java.util.List;

public class Batch {

    private List<MidPointObject> objects = new ArrayList<>();

    private int first;

    public List<MidPointObject> getObjects() {
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
