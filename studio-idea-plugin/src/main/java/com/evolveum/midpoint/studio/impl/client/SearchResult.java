package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.studio.impl.MidPointObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SearchResult {

    private List<MidPointObject> objects;

    public SearchResult() {
        this(null);
    }

    public SearchResult(List<MidPointObject> objects) {
        this.objects = objects;
    }

    public List<MidPointObject> getObjects() {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        return objects;
    }
}
