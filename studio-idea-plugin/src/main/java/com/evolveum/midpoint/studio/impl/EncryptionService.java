package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface EncryptionService {

    String NOTIFICATION_KEY = "Credentials";

    static EncryptionService getInstance(@NotNull Project project) {
        return project.getService(EncryptionService.class);
    }

    void init(String masterPassword);

    void changeMasterPassword(String oldPassword, String newPassword);

    boolean isAvailable();

    void refresh();

    <T extends EncryptedObject> List<T> list(Class<T> type);

    List<EncryptedObject> list();

    String add(EncryptedObject property);

    boolean delete(String key);

    EncryptedObject get(String key);

    <T extends EncryptedObject> T get(String key, Class<T> type);
}
