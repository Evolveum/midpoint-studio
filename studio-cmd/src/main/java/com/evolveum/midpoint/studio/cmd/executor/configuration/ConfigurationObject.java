package com.evolveum.midpoint.studio.cmd.executor.configuration;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class ConfigurationObject implements Serializable {

    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + " (" + getClass().getSimpleName() + ")";
    }
}
