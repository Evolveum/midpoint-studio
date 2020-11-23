package com.evolveum.midpoint.studio.impl.client;

import com.evolveum.midpoint.studio.impl.MidPointObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SearchResult {

    private List<MidPointObject> objects;

    public SearchResult(List<MidPointObject> objects) {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        
        this.objects = objects;
    }

    public List<MidPointObject> getObjects() {

        return objects;
    }
}
