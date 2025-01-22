package com.evolveum.midpoint.studio.impl;

import com.intellij.util.xmlb.annotations.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentSettings implements Serializable {

    private List<Environment> environments;
    private String selectedId;

    public List<Environment> getEnvironments() {
        if (environments == null) {
            environments = new ArrayList<>();
        }
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }

    @Transient
    public Environment getSelected() {
        for (Environment e : environments) {
            if (Objects.equals(e.getId(), selectedId)) {
                return e;
            }
        }
        return null;
    }

    @Deprecated
    public void setSelected(Environment selected) {
        if (selected == null) {
            this.selectedId = null;
            return;
        }

        this.selectedId = selected.getId();
    }

    public String getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
    }

    public static EnvironmentSettings createDefaultSettings() {
        EnvironmentSettings settings = new EnvironmentSettings();

        settings.getEnvironments().add(Environment.DEFAULT);
        settings.getEnvironments().add(Environment.DEMO);
        settings.setSelected(Environment.DEFAULT);

        return settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnvironmentSettings settings = (EnvironmentSettings) o;

        if (environments != null ? !environments.equals(settings.environments) : settings.environments != null)
            return false;
        return selectedId != null ? selectedId.equals(settings.selectedId) : settings.selectedId == null;
    }

    @Override
    public int hashCode() {
        int result = environments != null ? environments.hashCode() : 0;
        result = 31 * result + (selectedId != null ? selectedId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EnvironmentSettings{" +
                "selectedId='" + selectedId + '\'' +
                ", environments=" + environments +
                '}';
    }
}
