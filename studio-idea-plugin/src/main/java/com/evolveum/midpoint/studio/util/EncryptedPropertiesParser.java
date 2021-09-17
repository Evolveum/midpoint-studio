package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.impl.EncryptedProperty;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedPropertiesParser {

    private EnvironmentService environmentService;

    public EncryptedPropertiesParser(@NotNull EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    public List<EncryptedProperty> parse(File file) throws IOException {
        Properties props = new Properties();

        try (Reader r = new FileReader(file, StandardCharsets.UTF_8)) {
            props.load(r);
        }

        List<EncryptedProperty> properties = new ArrayList<>();

        // <envName, envUUID>
        Map<String, String> environments = new HashMap<>();

        for (int i = 0; ; i++) {
            String prefix = "property." + i + ".";

            String key = getProperty(props, prefix + "key");
            if (key == null) {
                break;
            }

            String value = getProperty(props, prefix + "value");
            String description = getProperty(props, prefix + "description");

            String environment = getProperty(props, prefix + "environment");
            String environmentUUID = resolveEnvironment(environment, environments);

            properties.add(new EncryptedProperty(key, environmentUUID, value, description));
        }

        return properties;
    }

    private String resolveEnvironment(String environment, Map<String, String> environments) {
        if (environment == null) {
            return null;
        }

        if (environments.containsKey(environment)) {
            return environments.get(environment);
        }

        String uuid = mapEnvironment(environment);
        environments.put(environment, uuid);

        return uuid;
    }

    public String getProperty(Properties properties, String key) {
        String value = properties.getProperty(key);

        if (value == null) {
            return null;
        }

        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    protected String mapEnvironment(String envName) {
        return null;
    }
}
