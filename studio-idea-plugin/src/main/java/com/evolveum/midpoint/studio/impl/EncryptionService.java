package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface EncryptionService {

    enum Status {

        UNKNOWN,

        OK,

        MISSING_FILE,

        PASSWORD_NOT_SET,

        PASSWORD_INCORRECT
    }

    class StatusMessage {

        private final Status status;

        private final String message;

        public StatusMessage(@NotNull Status status, String message) {
            this.status = status;
            this.message = message;
        }

        @NotNull
        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }


    String NOTIFICATION_KEY = "Credentials";

    static EncryptionService getInstance(@NotNull Project project) {
        return project.getService(EncryptionService.class);
    }

    void init(String masterPassword);

    void changeMasterPassword(String oldPassword, String newPassword);

    @NotNull
    StatusMessage getStatus();

    boolean isAvailable();

    void refresh();

    <T extends EncryptedObject> List<T> list(Class<T> type);

    List<EncryptedObject> list();

    String add(EncryptedObject property);

    boolean delete(String key);

    EncryptedObject get(String key);

    <T extends EncryptedObject> T get(String key, Class<T> type);
}
