package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptionServiceImpl implements EncryptionService {

    private static final Logger LOG = Logger.getInstance(EncryptionServiceImpl.class);

    private static final String CREDENTIALS_FILE_NAME = "credentials.kdbx";

    private static final String DATABASE_NAME = "Credentials";

    private Project project;

    private KeePassFile database;

    public EncryptionServiceImpl(@NotNull Project project) {
        this.project = project;

        LOG.info("Initializing " + getClass().getSimpleName());
    }

    @Override
    public void init(String masterPassword) {
        LOG.info("Initializing master password");

        if (StringUtils.isEmpty(masterPassword)) {
            return;
        }

        MidPointSettings settings = MidPointService.getInstance(project).getSettings();
        if (settings == null || StringUtils.isEmpty(settings.getProjectId())) {
            return;
        }

        MidPointUtils.setPassword(settings.getProjectId(), masterPassword);

        refresh(true, true);
    }

    @Override
    public void changeMasterPassword(String oldPassword, String newPassword) {
        LOG.info("Changing master password");

        MidPointSettings settings = MidPointService.getInstance(project).getSettings();
        if (settings == null || StringUtils.isEmpty(settings.getProjectId())) {
            throw new IllegalStateException("Midpoint setting unavailable");
        }

        if (StringUtils.isEmpty(newPassword)) {
            throw new IllegalArgumentException("New password must not be empty");
        }

        File dbFile = getDatabaseFile();
        if (!dbFile.exists()) {
            this.database = createKeePassBuilder(null).build();

            writeDatabase(newPassword);
        } else {
            String pwd = oldPassword != null ? oldPassword : newPassword;
            this.database = KeePassDatabase.getInstance(dbFile).openDatabase(pwd);
            writeDatabase(newPassword);
        }

        MidPointUtils.setPassword(settings.getProjectId(), newPassword);

        refresh(true, true);
    }

    @Override
    public boolean isAvailable() {
        String masterPassword = getMasterPassword();
        return StringUtils.isNoneEmpty(masterPassword);
    }

    @Override
    public synchronized void refresh() {
        refresh(true, true);
    }

    private void refresh(boolean create, boolean notificationIfFailure) {
        LOG.debug("Refreshing credentials create: " + create + ", notificationIfFailure: " + notificationIfFailure);

        String masterPassword = getMasterPassword();
        if (StringUtils.isEmpty(masterPassword)) {
            if (notificationIfFailure) {
                MidPointUtils.publishNotification(project, NOTIFICATION_KEY,
                        "Credentials file", "Master password not set. All encrypted values will be forgotten after " +
                                "restart, e.g. environment usernames/passwords, encrypted properties. ", NotificationType.WARNING,
                        new UpdateMasterPasswordNotificationAction(false));
            }
            return;
        }

        File dbFile = getDatabaseFile();
        if (!dbFile.exists() && create) {
            this.database = createKeePassBuilder(null).build();
            writeDatabase();
        }

        if (dbFile.exists()) {
            try {
                database = KeePassDatabase.getInstance(dbFile).openDatabase(masterPassword);
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(project, null, EncryptionService.class, NOTIFICATION_KEY,
                        "Couldn't open credentials database with master password", ex, new UpdateMasterPasswordNotificationAction(true));
            }
        }
    }

    @Override
    public List<EncryptedObject> list() {
        return list(EncryptedObject.class);
    }

    @Override
    public synchronized <T extends EncryptedObject> List<T> list(@NotNull Class<T> type) {
        LOG.debug("Listing entries, type: " + type);

        Group group = getTopGroup();
        if (group == null) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();
        for (Entry entry : group.getEntries()) {
            EncryptedObject property = createCredentials(entry);
            if (property.getClass().isAssignableFrom(type)) {
                list.add((T) property);
            }
        }

        return list;
    }

    @Override
    public synchronized String add(EncryptedObject credentials) {
        LOG.debug("Adding credentials ", credentials);

        Group group = getTopGroup();
        if (group == null) {
            return null;
        }

        Entry entry = credentials.buildEntry().build();

        Entry existing = group.getEntryByTitle(entry.getTitle());
        if (existing != null) {
            group.getEntries().remove(existing);
        }

        group.getEntries().add(entry);

        writeDatabase();

        return entry.getTitle();
    }

    @Override
    public synchronized boolean delete(String key) {
        LOG.debug("Deleting credentials with key ", key);

        Group group = getTopGroup();
        if (group == null) {
            return false;
        }

        Entry entry = group.getEntryByTitle(key);
        if (entry == null) {
            return false;
        }

        boolean deleted = group.getEntries().remove(entry);
        writeDatabase();

        return deleted;
    }

    @Override
    public <T extends EncryptedObject> T get(@NotNull String key, @NotNull Class<T> type) {
        LOG.debug("Getting encrypted key: " + key + ", type: " + type);
        Group group = getTopGroup();
        if (group == null) {
            return null;
        }

        Entry entry = group.getEntryByTitle(key);
        if (entry == null) {
            return null;
        }

        EncryptedObject property = createCredentials(entry);
        if (property.getClass().isAssignableFrom(type)) {
            return (T) property;
        }

        return null;
    }

    @Override
    public synchronized EncryptedObject get(@NotNull String key) {
        return get(key, EncryptedObject.class);
    }

    private Group getTopGroup() {
        if (database == null) {
            refresh(false, false);

            if (database == null) {
                return null;
            }
        }

        return database.getGroupByName(DATABASE_NAME);
    }

    private KeePassFileBuilder createKeePassBuilder(Meta meta) {
        if (meta == null) {
            meta = new MetaBuilder(DATABASE_NAME).historyMaxItems(10).build();
        }

        KeePassFileBuilder builder = new KeePassFileBuilder(meta);
        Group group = new GroupBuilder(DATABASE_NAME).build();
        builder.addTopGroups(group);

        return builder;
    }

    private File getDatabaseFile() {
        String basePath = project.getBasePath();
        return new File(basePath, CREDENTIALS_FILE_NAME);
    }

    private synchronized void writeDatabase() {
        String masterPassword = getMasterPassword();

        writeDatabase(masterPassword);

        refresh(false, true);
    }

    private synchronized void writeDatabase(String masterPassword) {
        if (database == null) {
            LOG.debug("Database is null, nothing to write");
            return;
        }

        File file = getDatabaseFile();

        try (OutputStream os = new FileOutputStream(file)) {
            KeePassDatabase.write(database, masterPassword, os);
        } catch (IOException ex) {
            MidPointUtils.publishExceptionNotification(project, null, EncryptionService.class, NOTIFICATION_KEY,
                    "Couldn't write credentials", ex);
        }
    }

    private String getMasterPassword() {
        MidPointSettings settings = MidPointService.getInstance(project).getSettings();
        if (settings == null || StringUtils.isEmpty(settings.getProjectId())) {
            LOG.debug("MidPoint settings not available.");
            return null;
        }

        return MidPointUtils.getPassword(settings.getProjectId());
    }

    private EncryptedObject createCredentials(Entry entry) {
        try {
            String title = entry.getTitle();
            Class<? extends EncryptedObject> type;
            if (title != null && title.matches("(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
                // we'll guess default type as credentials when title contains UUID. This is just for backwards compatibility
                type = EncryptedCredentials.class;
            } else {
                type = EncryptedProperty.class;
            }

            List<String> tags = entry.getTags();
            if (tags != null && tags.size() > 0) {
                String t = tags.get(0);

                type = (Class) getClass().getClassLoader().loadClass(t);
            }

            return type.getConstructor(Entry.class).newInstance(entry);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
