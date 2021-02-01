package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentProperties {

    private static final Logger LOG = Logger.getInstance(EnvironmentProperties.class);

    private Environment environment;

    private Properties properties;

    public EnvironmentProperties(@NotNull Environment environment) {
        this.environment = environment != null ? new Environment(environment) : null;

        load(environment);
    }

    private void load(Environment env) {
        LOG.debug("Reloading properties for new environment", env);

        Properties properties = new Properties();

        if (env == null || StringUtils.isEmpty(env.getPropertiesFilePath())) {
            this.properties = properties;
            return;
        }

        File file = new File(env.getPropertiesFilePath());
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            this.properties = properties;
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            MidPointUtils.publishExceptionNotification(env, EnvironmentProperties.class, EnvironmentService.NOTIFICATION_KEY,
                    "Couldn't load environment properties", ex);
        }

        this.properties = properties;
    }

    public String get(String key) {
        if (properties == null) {
            return null;
        }

        return properties.getProperty(key);
    }

    public Set<String> getKeys() {
        if (properties == null) {
            return new HashSet<>();
        }

        return (Set) properties.keySet();
    }
}
