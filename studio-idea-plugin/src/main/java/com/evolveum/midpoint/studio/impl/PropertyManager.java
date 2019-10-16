package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PropertyManager implements Listener {

    private static final Logger LOG = Logger.getInstance(PropertyManager.class);

    private Project project;

    private EnvironmentManager environmentManager;

    private Environment environment;

    private Properties properties;

    public PropertyManager(@NotNull Project project, @NotNull EnvironmentManagerImpl environmentManager) {
        this.project = project;
        this.environmentManager = environmentManager;

        environmentManager.addListener(this);
        reload(environmentManager.getSelected());
    }

    @Override
    public <T> void onEvent(Event<T> evt) {
        if (!EnvironmentManagerImpl.EVT_SELECTION_CHANGED.equals(evt.getId())) {
            LOG.debug("onEvent -> envt changed, but environment id is the same, skipping event");
            return;
        }

        reload((Environment) evt.getObject());
    }

    private void reload(Environment env) {
        LOG.debug("Reloading properties for new environment", env);

        if (Objects.equals(env, environment)) {
            return;
        }

        try {
            this.environment = env != null ? (Environment) BeanUtils.cloneBean(env) : null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

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
            // todo handle error
            throw new RuntimeException(ex);
        }

        this.properties = properties;
    }

    public String get(String key) {
        if (properties == null) {
            return null;
        }

        return properties.getProperty(key);
    }
}
