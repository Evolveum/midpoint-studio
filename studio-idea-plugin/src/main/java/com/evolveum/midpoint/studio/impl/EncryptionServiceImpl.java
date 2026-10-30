package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the decrypted credentials database in memory. Master password (read from keychain) and the
 * database are loaded once and cached together with the computed status. The cache is invalidated
 * when the credentials file changes on disk (VFS event or modification time check) or when the master
 * password changes.
 *
 * Reading keychain and opening the database are slow. Loading therefore never blocks EDT or a read
 * action: in those contexts a background load is scheduled and the current (possibly empty) cache
 * is returned. On other threads the load runs synchronously.
 */
public class EncryptionServiceImpl implements EncryptionService, Disposable {

    private static final Logger LOG = Logger.getInstance(EncryptionServiceImpl.class);

    private static final String CREDENTIALS_FILE_NAME = "credentials.kdbx";

    private static final long FILE_CHECK_INTERVAL = 1000L;

    private final Project project;

    private final Object lock = new Object();

    // all fields below are guarded by lock

    private CredentialsDatabase database;

    private String masterPassword;

    private boolean passwordLoaded;

    private boolean loaded;

    private boolean loading;

    private StatusMessage status = new StatusMessage(Status.UNKNOWN, "Credentials not loaded yet.");

    private long fileModified;

    private long fileLength;

    private long lastFileCheck;

    public EncryptionServiceImpl(@NotNull Project project) {
        this.project = project;

        LOG.info("Initializing " + getClass().getSimpleName());

        project.getMessageBus().connect(this).subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {

            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                String path = FileUtil.toSystemIndependentName(getDatabaseFile().getAbsolutePath());
                for (VFileEvent event : events) {
                    if (FileUtil.pathsEqual(path, event.getPath())) {
                        fileChangedOnDisk();
                        return;
                    }
                }
            }
        });
    }

    @Override
    public void dispose() {
    }

    @Override
    public void init(String masterPassword) {
        LOG.info("Initializing master password");

        if (StringUtils.isEmpty(masterPassword)) {
            return;
        }

        String projectId = getProjectId();
        if (projectId == null) {
            return;
        }

        MidPointUtils.setPassword(projectId, masterPassword);

        setCachedPassword(masterPassword);

        load(true, true, false);
    }

    @Override
    public void changeMasterPassword(String oldPassword, String newPassword) {
        LOG.info("Changing master password");

        String projectId = getProjectId();
        if (projectId == null) {
            throw new IllegalStateException("Midpoint setting unavailable");
        }

        if (StringUtils.isEmpty(newPassword)) {
            throw new IllegalArgumentException("New password must not be empty");
        }

        File dbFile = getDatabaseFile();

        CredentialsDatabase db;
        if (!dbFile.exists()) {
            db = CredentialsDatabase.create();
        } else {
            String pwd = oldPassword != null ? oldPassword : newPassword;
            try {
                db = CredentialsDatabase.read(dbFile, pwd);
            } catch (IOException ex) {
                throw new IllegalStateException("Couldn't open credentials file, reason: " + ex.getMessage(), ex);
            }
        }
        writeFile(db, newPassword);

        MidPointUtils.setPassword(projectId, newPassword);

        setCachedPassword(newPassword);

        load(true, true, false);
    }

    @Override
    public boolean isAvailable() {
        return getStatus().getStatus() == Status.OK;
    }

    @NotNull
    @Override
    public StatusMessage getStatus() {
        ensureLoaded();

        synchronized (lock) {
            return status;
        }
    }

    @Override
    public void refresh() {
        load(true, true, true);
    }

    @Override
    public void ensureLoaded() {
        checkFileChanged(false);

        synchronized (lock) {
            if (loaded) {
                return;
            }
        }

        if (canBlock()) {
            load(false, false, false);
        } else {
            scheduleLoad();
        }
    }

    @Override
    public List<EncryptedObject> list() {
        return list(EncryptedObject.class);
    }

    @Override
    public <T extends EncryptedObject> List<T> list(@NotNull Class<T> type) {
        LOG.debug("Listing entries, type: " + type);

        ensureLoaded();

        synchronized (lock) {
            if (database == null) {
                return new ArrayList<>();
            }

            List<T> list = new ArrayList<>();
            for (EncryptedObject property : database.list()) {
                if (property.getClass().isAssignableFrom(type)) {
                    list.add((T) property);
                }
            }

            return list;
        }
    }

    @Override
    public String add(EncryptedObject credentials) {
        LOG.debug("Adding credentials ", credentials);

        ensureLoadedForWrite();

        synchronized (lock) {
            if (database == null) {
                return null;
            }

            database.put(credentials);

            writeDatabase();

            return credentials.getKey();
        }
    }

    @Override
    public boolean delete(String key) {
        LOG.debug("Deleting credentials with key ", key);

        ensureLoadedForWrite();

        synchronized (lock) {
            if (database == null) {
                return false;
            }

            boolean deleted = database.remove(key);
            if (deleted) {
                writeDatabase();
            }

            return deleted;
        }
    }

    @Override
    public <T extends EncryptedObject> T get(@NotNull String key, @NotNull Class<T> type) {
        LOG.debug("Getting encrypted key: " + key + ", type: " + type);

        ensureLoaded();

        synchronized (lock) {
            if (database == null) {
                return null;
            }

            EncryptedObject property = database.get(key);
            if (property != null && property.getClass().isAssignableFrom(type)) {
                return (T) property;
            }

            return null;
        }
    }

    @Override
    public EncryptedObject get(@NotNull String key) {
        return get(key, EncryptedObject.class);
    }

    /**
     * Writes always work with fresh data, otherwise an external change of the file would be overwritten.
     * This may block on EDT, writes are rare user actions and master password is normally cached already.
     */
    private void ensureLoadedForWrite() {
        checkFileChanged(true);

        synchronized (lock) {
            if (loaded) {
                return;
            }
        }

        load(false, false, false);
    }

    private boolean canBlock() {
        Application app = ApplicationManager.getApplication();
        return !app.isDispatchThread() && !app.isReadAccessAllowed();
    }

    private void scheduleLoad() {
        synchronized (lock) {
            if (loading) {
                return;
            }
            loading = true;
        }

        LOG.debug("Scheduling credentials load in background");

        RunnableUtils.executeOnPooledThread(() -> {
            try {
                load(false, false, false);
            } finally {
                synchronized (lock) {
                    loading = false;
                }
            }
        });
    }

    private void fileChangedOnDisk() {
        File file = getDatabaseFile();
        long modified = file.lastModified();
        long length = file.length();

        synchronized (lock) {
            if (!loaded || (modified == fileModified && length == fileLength)) {
                // not loaded yet or this is our own write
                return;
            }

            LOG.info("Credentials file changed on disk, reloading");
            loaded = false;
        }

        scheduleLoad();
    }

    private void checkFileChanged(boolean force) {
        long now = System.currentTimeMillis();

        synchronized (lock) {
            if (!loaded || (!force && now - lastFileCheck < FILE_CHECK_INTERVAL)) {
                return;
            }
            lastFileCheck = now;
        }

        File file = getDatabaseFile();
        long modified = file.lastModified();
        long length = file.length();

        synchronized (lock) {
            if (loaded && (modified != fileModified || length != fileLength)) {
                LOG.info("Credentials file changed on disk, cache invalidated");
                loaded = false;
            }
        }
    }

    /**
     * Loads master password (if not cached or reloadPassword is set) and opens the database.
     * Runs on the calling thread.
     */
    private void load(boolean create, boolean notificationIfFailure, boolean reloadPassword) {
        LOG.debug("Loading credentials create: " + create + ", notificationIfFailure: " + notificationIfFailure
                + ", reloadPassword: " + reloadPassword);

        boolean readPassword;
        String password;
        synchronized (lock) {
            readPassword = !passwordLoaded || reloadPassword;
            password = masterPassword;
        }

        if (readPassword) {
            password = readMasterPassword();
        }

        File dbFile = getDatabaseFile();

        CredentialsDatabase db = null;
        StatusMessage newStatus;

        if (StringUtils.isEmpty(password)) {
            if (notificationIfFailure) {
                MidPointUtils.publishNotification(project, NOTIFICATION_KEY,
                        "Credentials file", "Master password not set. All encrypted values will be forgotten after " +
                                "restart, e.g. environment usernames/passwords, encrypted properties. ", NotificationType.WARNING,
                        new UpdateMasterPasswordNotificationAction(false));
            }

            newStatus = !dbFile.exists()
                    ? new StatusMessage(Status.MISSING_FILE, "Credentials file doesn't exist.")
                    : new StatusMessage(Status.PASSWORD_NOT_SET, "Master password not set.");
        } else {
            if (!dbFile.exists() && create) {
                writeFile(CredentialsDatabase.create(), password);
            }

            if (!dbFile.exists()) {
                newStatus = new StatusMessage(Status.MISSING_FILE, "Credentials file doesn't exist.");
            } else {
                try {
                    db = CredentialsDatabase.read(dbFile, password);
                    newStatus = new StatusMessage(Status.OK, "Credentials service is configured correctly.");
                } catch (Exception ex) {
                    RunnableUtils.rethrowIfCancellation(ex);

                    newStatus = new StatusMessage(Status.PASSWORD_INCORRECT,
                            "Couldn't open credentials file, reason: " + ex.getMessage() + ".");

                    if (notificationIfFailure) {
                        MidPointUtils.publishExceptionNotification(project, null, EncryptionService.class, NOTIFICATION_KEY,
                                "Couldn't open credentials database with master password", ex,
                                new UpdateMasterPasswordNotificationAction(true));
                    }
                }
            }
        }

        long modified = dbFile.lastModified();
        long length = dbFile.length();

        synchronized (lock) {
            if (readPassword) {
                masterPassword = password;
                passwordLoaded = true;
            }
            database = db;
            status = newStatus;
            loaded = true;
            fileModified = modified;
            fileLength = length;
            lastFileCheck = System.currentTimeMillis();
        }

        LOG.debug("Credentials loaded, status: " + newStatus.getStatus());
    }

    private void setCachedPassword(String password) {
        synchronized (lock) {
            masterPassword = password;
            passwordLoaded = true;
            loaded = false;
        }
    }

    private File getDatabaseFile() {
        String basePath = project.getBasePath();
        return new File(basePath, CREDENTIALS_FILE_NAME);
    }

    /**
     * Writes in-memory database using cached master password and reloads it from disk. Called under lock.
     */
    private void writeDatabase() {
        String password = masterPassword;
        if (StringUtils.isEmpty(password)) {
            password = readMasterPassword();
            masterPassword = password;
            passwordLoaded = true;
        }

        writeFile(database, password);

        loaded = false;
        load(false, true, false);
    }

    private void writeFile(CredentialsDatabase db, String masterPassword) {
        if (db == null) {
            LOG.debug("Database is null, nothing to write");
            return;
        }

        try {
            db.write(getDatabaseFile(), masterPassword);
        } catch (IOException ex) {
            MidPointUtils.publishExceptionNotification(project, null, EncryptionService.class, NOTIFICATION_KEY,
                    "Couldn't write credentials", ex);
        }
    }

    private String getProjectId() {
        MidPointConfiguration settings = MidPointService.get(project).getSettings();
        if (settings == null || StringUtils.isEmpty(settings.getProjectId())) {
            LOG.debug("MidPoint settings not available.");
            return null;
        }

        return settings.getProjectId();
    }

    private String readMasterPassword() {
        String projectId = getProjectId();
        if (projectId == null) {
            return null;
        }

        return MidPointUtils.getPassword(projectId);
    }
}
