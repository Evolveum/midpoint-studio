package com.evolveum.midpoint.studio.impl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.io.NonClosingInputStream;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;
import org.linguafranca.pwdb.kdbx.jackson.KdbxEntryTags;
import org.linguafranca.pwdb.kdbx.jackson.KdbxSerializableDatabase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * KeePass (kdbx) file holding encrypted objects as entries of the root group, one entry per key (title).
 * Object type is stored in entry tags.
 *
 * Files written by previously used openkeepass library are read as well. Those are KDBX 3 files without
 * header hash in XML and with group fields serialized directly in Root element, strict KeePassJava2 reader
 * refuses them. Files are always written in KDBX 4 format with fresh encryption parameters.
 */
public class CredentialsDatabase {

    public static final String DATABASE_NAME = "Credentials";

    private static final String UUID_PATTERN = "(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";

    static {
        KdbxSerializableDatabase.FAIL_ON_UNKNOWN_PROPERTIES = false;
    }

    private final KdbxDatabase database;

    private CredentialsDatabase(KdbxDatabase database) {
        this.database = database;
    }

    public static CredentialsDatabase create() {
        KdbxDatabase database = new KdbxDatabase();
        database.setName(DATABASE_NAME);
        database.getRootGroup().setName(DATABASE_NAME);

        return new CredentialsDatabase(database);
    }

    public static CredentialsDatabase read(@NotNull File file, @NotNull String password) throws IOException {
        byte[] key = password.getBytes(StandardCharsets.UTF_8);

        KdbxDatabase database;
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            database = KdbxDatabase.read(new KdbxCredentials(key), is);
        } catch (IllegalStateException ex) {
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                database = readLegacy(key, is);
            }
        }

        return new CredentialsDatabase(database);
    }

    /**
     * Same as {@link KdbxStreamFormat#read}, except header hash is verified only when the file contains it.
     */
    private static KdbxDatabase readLegacy(byte[] key, InputStream is) throws IOException {
        KdbxHeader header = new KdbxHeader();
        try (InputStream decrypted = KdbxSerializer.createUnencryptedInputStream(
                new KdbxCredentials(key), header, new NonClosingInputStream(is))) {

            KdbxSerializableDatabase serializable = new KdbxSerializableDatabase();
            serializable.setEncryption(header.getInnerStreamEncryptor());
            serializable.read(decrypted);

            byte[] headerHash = serializable.getHeaderHash();
            if (header.getVersion() == 3 && headerHash != null && !Arrays.equals(headerHash, header.getHeaderHash())) {
                throw new IllegalStateException("Header hash does not match");
            }

            return new KdbxDatabase(serializable.keePassFile, new KdbxStreamFormat(header));
        }
    }

    public void write(@NotNull File file, @NotNull String password) throws IOException {
        byte[] key = password.getBytes(StandardCharsets.UTF_8);

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            database.write(new KdbxStreamFormat(new KdbxHeader(4)), new KdbxCredentials(key), os);
        }
    }

    public List<EncryptedObject> list() {
        List<EncryptedObject> list = new ArrayList<>();
        for (Entry entry : getGroup().getEntries()) {
            list.add(createObject(entry));
        }

        return list;
    }

    @Nullable
    public EncryptedObject get(@NotNull String key) {
        Entry entry = findEntry(key);
        return entry != null ? createObject(entry) : null;
    }

    /**
     * Adds object, existing object with the same key is replaced.
     */
    public void put(@NotNull EncryptedObject object) {
        Group group = getGroup();

        Entry existing = findEntry(object.getKey());
        if (existing != null) {
            group.removeEntry(existing);
        }

        Entry entry = database.newEntry();
        object.fillEntry(entry);
        KdbxEntryTags.set(entry, object.getClass().getName());

        group.addEntry(entry);
    }

    public boolean remove(@NotNull String key) {
        Entry entry = findEntry(key);
        if (entry == null) {
            return false;
        }

        getGroup().removeEntry(entry);

        return true;
    }

    private Group getGroup() {
        return database.getRootGroup();
    }

    private Entry findEntry(String key) {
        for (Entry entry : getGroup().getEntries()) {
            if (StringUtils.equals(key, entry.getTitle())) {
                return entry;
            }
        }

        return null;
    }

    private EncryptedObject createObject(Entry entry) {
        try {
            Class<? extends EncryptedObject> type;

            String tags = KdbxEntryTags.get(entry);
            if (StringUtils.isNotEmpty(tags)) {
                type = Class.forName(tags, true, getClass().getClassLoader()).asSubclass(EncryptedObject.class);
            } else {
                // entries written before tags were introduced, credentials have UUID as a key
                String title = entry.getTitle();
                type = title != null && title.matches(UUID_PATTERN) ? EncryptedCredentials.class : EncryptedProperty.class;
            }

            return type.getConstructor(Entry.class).newInstance(entry);
        } catch (Exception ex) {
            throw new IllegalStateException("Couldn't create object from entry " + entry.getTitle(), ex);
        }
    }
}
