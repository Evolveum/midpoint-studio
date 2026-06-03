package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.util.LocalizationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * Application-scoped service for translating MidPoint schema localization keys.
 * Loads translations from localization/schema.properties on classpath (provided by midpoint-localization).
 */
@Service(Service.Level.APP)
public final class LocalizationService {

    private static final Logger LOG = Logger.getInstance(LocalizationService.class);

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private static final String SCHEMA_PROPERTIES = "localization/schema.properties";

    private final Properties properties = new Properties();

    public LocalizationService() {
        loadProperties(SCHEMA_PROPERTIES);
    }

    public static @NotNull LocalizationService get() {
        return ApplicationManager.getApplication().getService(LocalizationService.class);
    }

    private void loadProperties(String resource) {
        try (InputStream is = LocalizationService.class.getClassLoader().getResourceAsStream(resource)) {
            if (is == null) {
                LOG.warn("Localization resource not found on classpath: " + resource);
                return;
            }

            properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            LOG.info("Loaded " + properties.size() + " entries from " + resource);
        } catch (IOException ex) {
            LOG.error("Couldn't load localization resource " + resource, ex);
        }
    }

    public String translate(@NotNull String key, Object... params) {
        String pattern = properties.getProperty(key);
        if (pattern == null) {
            return key;
        }

        if (params == null || params.length == 0) {
            return pattern;
        }

        return new MessageFormat(pattern, DEFAULT_LOCALE).format(params);
    }

    public <T extends Enum> String translate(T value) {
        if (value == null) {
            return null;
        }

        String key = LocalizationUtil.createKeyForEnum(value);
        return translate(key);
    }
}
