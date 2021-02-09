package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.impl.EncryptedProperty;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedPropertiesSerializer {

    private EnvironmentService environmentService;

    public EncryptedPropertiesSerializer(@NotNull EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    public void serialize(List<EncryptedProperty> properties, File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }

        file.createNewFile();

        Properties props = new Properties() {

            /**
             * not very nice way to make properties sorted when stored to file
             */
            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {
                Set<Map.Entry<Object, Object>> set = new TreeSet<>((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare((String) o1.getKey(), (String) o2.getKey()));

                set.addAll(super.entrySet());

                return set;
            }
        };

        int i = 0;
        for (EncryptedProperty property : properties) {
            String prefix = "property." + i + ".";

            putProperty(props, prefix + "key", property.getKey());
            putProperty(props, prefix + "value", property.getValue());
            putProperty(props, prefix + "description", property.getDescription());

            if (property.getEnvironment() != null) {
                Environment env = environmentService.get(property.getEnvironment());
                if (env != null) {
                    putProperty(props, prefix + "environment", env.getName());
                }
            }

            i++;
        }

        try (Writer w = new FileWriter(file, StandardCharsets.UTF_8)) {
            props.store(w, null);
        }
    }

    private void putProperty(Properties properties, String key, String value) {
        if (properties == null || key == null || value == null) {
            return;
        }

        properties.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
