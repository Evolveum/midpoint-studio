package com.evolveum.midpoint.studio.impl.service;

import com.evolveum.midpoint.studio.impl.MidPointException;
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
 * Created by Viliam Repan (lazyman).
 */
public class MidPointLocalizationService {

    private static final Logger LOG = Logger.getInstance(MidPointLocalizationService.class);

    private List<Properties> properties = new ArrayList<>();

    public MidPointLocalizationService() {
        LOG.info("Initializing " + getClass().getSimpleName());

        init();
    }

    private void init() {
        loadProperties("/localization/MidPoint.properties");
        loadProperties("/localization/schema.properties");
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
