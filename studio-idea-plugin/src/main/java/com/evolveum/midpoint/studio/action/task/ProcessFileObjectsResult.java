package com.evolveum.midpoint.studio.action.task;

import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProcessFileObjectsResult {

    private List<String> newObjects;

    private boolean stop;

    public ProcessFileObjectsResult() {
        this(Collections.emptyList(), false);
    }

    public ProcessFileObjectsResult(List<String> newObjects, boolean stop) {
        this.newObjects = newObjects;
        this.stop = stop;
    }

    public List<String> getNewObjects() {
        return newObjects;
    }

    public boolean isStop() {
        return stop;
    }
}
