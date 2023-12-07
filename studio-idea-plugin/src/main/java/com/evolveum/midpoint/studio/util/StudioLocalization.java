package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.impl.MidPointException;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

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
 * todo create singleton from this instead of service!
 * todo this should probably also implement {@link com.evolveum.midpoint.common.LocalizationService}
 */
public class StudioLocalization {

    private static final Logger LOG = Logger.getInstance(StudioLocalization.class);

    private static final StudioLocalization INSTANCE = new StudioLocalization();

    public static StudioLocalization get() {
        return INSTANCE;
    }

    private final List<Properties> properties = new ArrayList<>();

    public StudioLocalization() {
        LOG.info("Initializing " + getClass().getSimpleName());

        init();
    }

    private void init() {
        loadProperties("/messages/MidPointStudio.properties");
        loadProperties("/localization/MidPoint.properties");
        loadProperties("/localization/schema.properties");
    }

    private void loadProperties(String resource) {
        Properties properties = new Properties();

        try (InputStream is = StudioLocalization.class.getResourceAsStream(resource)) {
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

    @NotNull
    public static String message(@NotNull
                                 @PropertyKey(resourceBundle = "messages.MidPointStudio")
                                 String key, Object... params) {
        return get().translate(key);
    }

}
