package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface CredentialsService {

    String NOTIFICATION_KEY = "Credentials";

    static CredentialsService getInstance(@NotNull Project project) {
        return project.getService(CredentialsService.class);
    }

    void init(String masterPassword);

    void changeMasterPassword(String oldPassword, String newPassword);

    boolean isAvailable();

    void refresh();

    List<Credentials> list();

    String add(Credentials credentials);

    boolean delete(String key);

    Credentials get(String key);
}
