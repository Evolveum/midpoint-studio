package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.traces.OpNodeTreeBuilder;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceDictionaryEntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceDictionaryType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.collections4.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Experimental
public class StudioNameResolver implements OpNodeTreeBuilder.NameResolver {

    private static final String NAMES_FILE = "names.txt";

    protected final Map<String, Set<String>> objectMap = new HashMap<>();

    public StudioNameResolver(TraceDictionaryType dictionary, VirtualFile file) {
        if (dictionary != null) {
            readFromDictionary(dictionary);
        }
        if (file != null) {
            readFromFile(file);
        }
    }

    private void readFromFile(VirtualFile file) {
        VirtualFile namesFile = file.getParent().findChild(NAMES_FILE);
        if (namesFile != null && namesFile.exists()) {
            System.out.println("Reading names from " + namesFile);
            try {
                readFromStream(namesFile.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readFromStream(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        for (;;) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.startsWith("#")) {
                continue;
            }
            int sep = line.indexOf(';');
            if (sep < 0) {
                System.out.println("Ignoring: " + line);
                continue;
            }
            String oid = line.substring(0, sep);
            String name = line.substring(sep + 1).trim();
            add(oid, name);
        }
        stream.close();
    }

    private void readFromDictionary(TraceDictionaryType dictionary) {
        for (TraceDictionaryEntryType entry : dictionary.getEntry()) {
            PrismObject<?> object = entry != null && entry.getObject() != null ? entry.getObject().getObject() : null;
            if (object != null) {
                add(object.getOid(), PolyString.getOrig(object.getName()));
            }
        }
    }

    private void add(String oid, String name) {
        Set<String> names = objectMap.computeIfAbsent(oid, s -> new HashSet<>());
        CollectionUtils.addIgnoreNull(names, name);
    }

    @Override
    public PolyStringType getName(String oid) {
        Set<String> names = objectMap.get(oid);
        if (names == null || names.isEmpty()) {
            return null;
        } else {
            return PolyStringType.fromOrig(String.join(", ", names));
        }
    }
}
