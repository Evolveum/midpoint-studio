package com.evolveum.midpoint.studio.impl.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ScriptObjects implements Serializable {

    private List<ScriptObject> scripts;

    public List<ScriptObject> getScripts() {
        if (scripts == null) {
            scripts = new ArrayList<>();
        }
        return scripts;
    }

    public void setScripts(List<ScriptObject> scripts) {
        this.scripts = scripts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScriptObjects that = (ScriptObjects) o;

        return scripts != null ? scripts.equals(that.scripts) : that.scripts == null;
    }

    @Override
    public int hashCode() {
        return scripts != null ? scripts.hashCode() : 0;
    }
}
