package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Service that simplifies localization of MidPoint related keys.
 *
 * Created by Viliam Repan (lazyman).
 */
public class MidPointLocalizationService {

    private List<Properties> properties = new ArrayList<>();

    public MidPointLocalizationService() {
        init();
    }

    private void init() {
        loadProperties("/localization/MidPoint.properties");
        loadProperties("/localization/schema.properties");
    }

    public static MidPointLocalizationService getInstance() {
        Application application = ApplicationManager.getApplication();
        return application.getComponent(MidPointLocalizationService.class);
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
