package com.evolveum.midpoint.studio.impl.service;

import com.evolveum.midpoint.studio.impl.MidPointException;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Service that simplifies localization of MidPoint related keys.
 * <p>
 * todo put together with {@link com.evolveum.midpoint.studio.util.StudioBundle}, create singleton from this instead of service!
 * todo this should probably also implement {@link com.evolveum.midpoint.common.LocalizationService}
 */
public class MidPointLocalizationService {

    private static final Logger LOG = Logger.getInstance(MidPointLocalizationService.class);

    private static final MidPointLocalizationService INSTANCE = new MidPointLocalizationService();

    public static MidPointLocalizationService get() {
        return INSTANCE;
    }

    private final List<Properties> properties = new ArrayList<>();

    public MidPointLocalizationService() {
        LOG.info("Initializing " + getClass().getSimpleName());

        init();
    }

    public static MidPointLocalizationService getInstance() {
        return get();
    }

    private void init() {
        loadProperties("/localization/MidPoint.properties");
        loadProperties("/localization/schema.properties");
        loadProperties("/messages/MidPointStudio.properties");
    }

    private void loadProperties(String resource) {
        Properties properties = new Properties();

        try (InputStream is = MidPointLocalizationService.class.getResourceAsStream(resource)) {
            if (is == null) {
                return;
            }

            properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new MidPointException("Couldn't load properties resource " + resource, ex);
        }

        this.properties.add(properties);
    }

    public String translateEnum(Enum<?> e) {
        return translateEnum(e, null);
    }

    public String translateEnum(Enum<?> e, String nullKey) {
        if (e == null) {
            return nullKey != null ? translate(nullKey) : null;
        }

        String key = MidPointUtils.createKeyForEnum(e);
        return translate(key);
    }

    public String translate(String key) {
        return translate(key, null);
    }

    public String translate(String key, String defaultValue) {
        for (Properties properties : this.properties) {
            String value = properties.getProperty(key);
            if (value != null) {
                return value;
            }
        }

        return defaultValue;
    }
}
