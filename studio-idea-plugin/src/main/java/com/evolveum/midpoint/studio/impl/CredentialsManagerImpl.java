package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.util.MidPointUtils;
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
public class CredentialsManagerImpl implements CredentialsManager {

    private static final Logger LOG = Logger.getInstance(CredentialsManagerImpl.class);

    private static final String CREDENTIALS_FILE_NAME = "credentials.kdbx";

    private static final String DATABASE_NAME = "Credentials";

    private Project project;

    private KeePassFile database;

    public CredentialsManagerImpl(@NotNull Project project) {
        this.project = project;

        LOG.info("Initializing " + getClass().getSimpleName());

        refresh();
    }

    @Override
    public synchronized void refresh() {
        refresh(true);
    }

    private void refresh(boolean create) {
        LOG.debug("Refreshing credentials create=", create);

        String masterPassword = getMasterPassword();
        if (StringUtils.isEmpty(masterPassword)) {
            return;
        }

        File dbFile = getDatabaseFile();
        if (!dbFile.exists() && create) {
            this.database = createKeePassBuilder(null).build();
            writeDatabase();
        }

        if (dbFile.exists()) {
            database = KeePassDatabase.getInstance(dbFile).openDatabase(masterPassword);
        }
    }

    @Override
    public synchronized List<Credentials> list() {
        Group group = getTopGroup();
        if (group == null) {
            return new ArrayList<>();
        }

        List<Credentials> list = new ArrayList<>();
        for (Entry entry : group.getEntries()) {
            list.add(createCredentials(entry));
        }

        return list;
    }

    @Override
    public synchronized String add(Credentials credentials) {
        LOG.debug("Adding credentials ", credentials);

        Group group = getTopGroup();
        if (group == null) {
            return null;
        }

        Entry entry = new EntryBuilder()
                .title(credentials.getKey())
                .username(credentials.getUsername())
                .password(credentials.getPassword())
                .notes(credentials.getDescription())
                .build();

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
    public synchronized Credentials get(String key) {
        Group group = getTopGroup();
        if (group == null) {
            return null;
        }

        Entry entry = group.getEntryByTitle(key);
        return entry != null ? createCredentials(entry) : null;
    }

    private Group getTopGroup() {
        if (database == null) {
            return null;
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
        File file = getDatabaseFile();

        String masterPassword = getMasterPassword();

        try (OutputStream os = new FileOutputStream(file)) {
            KeePassDatabase.write(database, masterPassword, os);
        } catch (IOException ex) {
            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Couldn't write credentials", ex);
        }

        refresh(false);
    }

    private String getMasterPassword() {
        MidPointSettings settings = MidPointManager.getInstance(project).getSettings();
        if (settings == null || StringUtils.isEmpty(settings.getProjectId())) {
            return null;
        }

        return MidPointUtils.getPassword(settings.getProjectId());
    }

    private Credentials createCredentials(Entry entry) {
        return new Credentials(entry.getTitle(),
                entry.getUrl(),
                entry.getUsername(),
                entry.getPassword(),
                entry.getNotes());
    }
}
