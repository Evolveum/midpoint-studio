package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface EnvironmentManager extends Stateful<EnvironmentSettings> {

    static EnvironmentManager getInstance(@NotNull Project project) {
        return project.getComponent(EnvironmentManager.class);
    }

    EnvironmentSettings getFullSettings();

    List<Environment> getEnvironments();

    Environment getSelected();

    void select(String id);

    String add(Environment env);

    boolean delete(String id);

    Environment get(String id);
}
