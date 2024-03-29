package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.Stateful;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface EnvironmentService extends Stateful<EnvironmentSettings> {

    String NOTIFICATION_KEY = "Environment";

    static EnvironmentService getInstance(@NotNull Project project) {
        return project.getService(EnvironmentService.class);
    }

    EnvironmentSettings getFullSettings();

    List<Environment> getEnvironments();

    boolean isEnvironmentSelected();

    Environment getSelected();

    void select(String id);

    String add(Environment env);

    String modify(Environment env);

    boolean delete(String id);

    Environment get(String id);

    EnvironmentProperties getSelectedEnvironmentProperties();
}
