package com.evolveum.midpoint.studio.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Covers migration from openkeepass to KeePassJava2. Fixture credentials-openkeepass.kdbx was written by
 * openkeepass 0.8.1 the same way the plugin used to write it, it must be read, modified and written by
 * the new library. The fixture was created with this code (master password is {@link #PASSWORD}):
 *
 * <pre>
 * Meta meta = new MetaBuilder("Credentials").historyMaxItems(10).build();
 * KeePassFileBuilder builder = new KeePassFileBuilder(meta);
 * Group group = new GroupBuilder("Credentials").build();
 * builder.addTopGroups(group);
 *
 * group.getEntries().add(new EntryBuilder()
 *         .title(ENV).url(ENV).notes("Environment credentials")
 *         .tags(List.of("com.evolveum.midpoint.studio.impl.EncryptedCredentials"))
 *         .username("administrator").password("Test5ecr3t!").build());
 * group.getEntries().add(new EntryBuilder()
 *         .title(ENV + "-proxy").url(ENV).notes("Proxy credentials")
 *         .tags(List.of("com.evolveum.midpoint.studio.impl.EncryptedCredentials"))
 *         .username("proxyuser").password("pr0xy").build());
 * group.getEntries().add(new EntryBuilder()
 *         .title("db.password").url(ENV).notes("Database password")
 *         .tags(List.of("com.evolveum.midpoint.studio.impl.EncryptedProperty"))
 *         .password("p@ss w0rd &lt;&gt;&amp;\"' \u010d\u0161\u017e").build());
 * group.getEntries().add(new EntryBuilder()
 *         .title("ldap.bind.password")
 *         .tags(List.of("com.evolveum.midpoint.studio.impl.EncryptedProperty"))
 *         .password("global-value").build());
 * // entries written before tags were introduced
 * group.getEntries().add(new EntryBuilder()
 *         .title(LEGACY_ENV).url(LEGACY_ENV).username("legacy-admin").password("legacy-pwd").build());
 * group.getEntries().add(new EntryBuilder()
 *         .title("legacy.property").password("legacy-value").build());
 *
 * KeePassDatabase.write(builder.build(), PASSWORD, outputStream);
 * </pre>
 */
public class CredentialsDatabaseTest {

    private static final String PASSWORD = "studio-test-password";

    private static final String ENV = "5f3a4c0e-1d2b-4e6f-9a8b-7c6d5e4f3a2b";

    private static final String LEGACY_ENV = "0c4b9d2a-7e1f-4a3c-8b5d-2f6e1a9c3b7d";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File oldFile;

    @Before
    public void setUp() throws IOException {
        oldFile = folder.newFile("credentials.kdbx");
        try (InputStream is = getClass().getResourceAsStream("/credentials/credentials-openkeepass.kdbx")) {
            assertNotNull("fixture not found", is);
            Files.copy(is, oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    public void testReadFileWrittenByOpenkeepass() throws IOException {
        assertEquals(3, kdbxMajorVersion(oldFile));

        CredentialsDatabase db = CredentialsDatabase.read(oldFile, PASSWORD);

        assertOpenkeepassContent(db);
    }

    @Test
    public void testRewriteFileWrittenByOpenkeepass() throws IOException {
        CredentialsDatabase db = CredentialsDatabase.read(oldFile, PASSWORD);
        List<EncryptedObject> before = db.list();

        File newFile = folder.newFile("rewritten.kdbx");
        db.write(newFile, PASSWORD);

        assertEquals(4, kdbxMajorVersion(newFile));

        CredentialsDatabase reread = CredentialsDatabase.read(newFile, PASSWORD);
        assertOpenkeepassContent(reread);
        assertEquals(before, reread.list());
    }

    @Test
    public void testOverwriteFileWrittenByOpenkeepassInPlace() throws IOException {
        CredentialsDatabase db = CredentialsDatabase.read(oldFile, PASSWORD);
        db.write(oldFile, PASSWORD);

        assertEquals(4, kdbxMajorVersion(oldFile));
        assertOpenkeepassContent(CredentialsDatabase.read(oldFile, PASSWORD));
    }

    @Test
    public void testModifyFileWrittenByOpenkeepass() throws IOException {
        CredentialsDatabase db = CredentialsDatabase.read(oldFile, PASSWORD);

        String newEnv = "9b1e2f3a-4c5d-4e6f-8a9b-0c1d2e3f4a5b";
        db.put(new EncryptedCredentials(newEnv, newEnv, "newuser", "newpass", "New environment"));
        db.put(new EncryptedProperty("db.password", ENV, "changed", "Database password"));
        assertTrue(db.remove("legacy.property"));
        assertFalse(db.remove("legacy.property"));

        db.write(oldFile, PASSWORD);

        CredentialsDatabase reread = CredentialsDatabase.read(oldFile, PASSWORD);
        assertEquals(6, reread.list().size());

        assertEquals(new EncryptedCredentials(newEnv, newEnv, "newuser", "newpass", "New environment"), reread.get(newEnv));
        assertEquals(new EncryptedProperty("db.password", ENV, "changed", "Database password"), reread.get("db.password"));
        assertNull(reread.get("legacy.property"));

        // untouched entries survive
        assertEquals(new EncryptedCredentials(ENV, ENV, "administrator", "Test5ecr3t!", "Environment credentials"),
                reread.get(ENV));
        assertEquals(new EncryptedCredentials(LEGACY_ENV, LEGACY_ENV, "legacy-admin", "legacy-pwd", null),
                reread.get(LEGACY_ENV));
    }

    @Test
    public void testChangePasswordOfFileWrittenByOpenkeepass() throws IOException {
        CredentialsDatabase db = CredentialsDatabase.read(oldFile, PASSWORD);
        db.write(oldFile, "new-password");

        assertOpenkeepassContent(CredentialsDatabase.read(oldFile, "new-password"));
        assertThrows(Exception.class, () -> CredentialsDatabase.read(oldFile, PASSWORD));
    }

    @Test
    public void testWrongPassword() throws IOException {
        assertThrows(Exception.class, () -> CredentialsDatabase.read(oldFile, "wrong"));

        File newFile = folder.newFile("new.kdbx");
        CredentialsDatabase.read(oldFile, PASSWORD).write(newFile, PASSWORD);
        assertThrows(Exception.class, () -> CredentialsDatabase.read(newFile, "wrong"));
    }

    @Test
    public void testCreateNewDatabase() throws IOException {
        CredentialsDatabase db = CredentialsDatabase.create();
        assertTrue(db.list().isEmpty());

        db.put(new EncryptedProperty("key", null, "value", null));
        db.put(new EncryptedCredentials(ENV, ENV, "user", "pwd", "desc"));

        File file = folder.newFile("new.kdbx");
        db.write(file, PASSWORD);

        CredentialsDatabase reread = CredentialsDatabase.read(file, PASSWORD);
        assertEquals(2, reread.list().size());
        assertEquals(new EncryptedProperty("key", null, "value", null), reread.get("key"));
        assertEquals(new EncryptedCredentials(ENV, ENV, "user", "pwd", "desc"), reread.get(ENV));
        assertNull(reread.get("missing"));
    }

    private void assertOpenkeepassContent(CredentialsDatabase db) {
        List<EncryptedObject> list = db.list();
        assertEquals(6, list.size());

        EncryptedObject obj = db.get(ENV);
        assertEquals(EncryptedCredentials.class, obj.getClass());
        assertEquals(new EncryptedCredentials(ENV, ENV, "administrator", "Test5ecr3t!", "Environment credentials"), obj);

        obj = db.get(ENV + "-proxy");
        assertEquals(EncryptedCredentials.class, obj.getClass());
        assertEquals(new EncryptedCredentials(ENV + "-proxy", ENV, "proxyuser", "pr0xy", "Proxy credentials"), obj);

        obj = db.get("db.password");
        assertEquals(EncryptedProperty.class, obj.getClass());
        assertEquals(new EncryptedProperty("db.password", ENV, "p@ss w0rd <>&\"' čšž", "Database password"), obj);

        obj = db.get("ldap.bind.password");
        assertEquals(EncryptedProperty.class, obj.getClass());
        assertEquals(new EncryptedProperty("ldap.bind.password", null, "global-value", null), obj);

        // entries without tags, type guessed from key
        obj = db.get(LEGACY_ENV);
        assertEquals(EncryptedCredentials.class, obj.getClass());
        assertEquals(new EncryptedCredentials(LEGACY_ENV, LEGACY_ENV, "legacy-admin", "legacy-pwd", null), obj);

        obj = db.get("legacy.property");
        assertEquals(EncryptedProperty.class, obj.getClass());
        assertEquals(new EncryptedProperty("legacy.property", null, "legacy-value", null), obj);
    }

    /**
     * KDBX header starts with two signatures followed by minor and major version (little endian).
     */
    private static int kdbxMajorVersion(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return ByteBuffer.wrap(bytes, 10, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }
}
