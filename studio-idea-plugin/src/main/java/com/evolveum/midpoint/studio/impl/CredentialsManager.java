package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface CredentialsManager {

    static CredentialsManager getInstance(@NotNull Project project) {
        return project.getComponent(CredentialsManager.class);
    }

    void refresh();

    List<Credentials> list();

    String add(Credentials credentials);

    boolean delete(String key);

    Credentials get(String key);
}
