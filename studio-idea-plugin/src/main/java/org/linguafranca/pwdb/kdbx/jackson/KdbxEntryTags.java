package org.linguafranca.pwdb.kdbx.jackson;

import org.linguafranca.pwdb.Entry;

/**
 * Accessor for entry tags. KeePassJava2 reads and writes them but doesn't expose them in its API,
 * the class lives in library package to reach the protected field.
 */
public final class KdbxEntryTags {

    private KdbxEntryTags() {
    }

    public static String get(Entry entry) {
        return entry instanceof KdbxEntry kdbxEntry ? kdbxEntry.tags : null;
    }

    public static void set(Entry entry, String tags) {
        if (entry instanceof KdbxEntry kdbxEntry) {
            kdbxEntry.tags = tags;
        }
    }
}
