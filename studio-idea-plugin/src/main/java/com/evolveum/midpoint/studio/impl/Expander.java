package com.evolveum.midpoint.studio.impl;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Expander {

    private static final Pattern PATTERN = Pattern.compile("\\$\\((\\S*?)\\)");

    private Environment environment;

    private EncryptionService encryptionService;

    private EnvironmentProperties environmentProperties;

    public Expander(Environment environment, EncryptionService encryptionService) {
        this.environment = environment;
        this.encryptionService = encryptionService;
        this.environmentProperties = new EnvironmentProperties(environment);
    }

    /**
     * Options that will be translated
     * $(KEY)
     * $(username:KEY)
     * $(password:KEY)
     *
     * @param object
     * @return
     */
    public String expand(String object) {
        if (object == null) {
            return null;
        }

        Matcher matcher = PATTERN.matcher(object);

        List<String> missingKeys = new ArrayList<>();

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (key.isEmpty()) {
                matcher.appendReplacement(sb, "");
                continue;
            }

            String value = expandKey(key);
            if (value == null) {
                matcher.appendReplacement(sb, "\\" + matcher.group());
                missingKeys.add(key);
            } else {
                matcher.appendReplacement(sb, value);
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public InputStream expand(InputStream is, Charset charset) throws IOException {
        if (is == null) {
            return null;
        }

        String text = IOUtils.toString(is, charset);
        String expanded = expand(text);

        return new ByteArrayInputStream(expanded.getBytes());
    }

    private String expandKey(String key) {
        String value = null;
        if (encryptionService == null || !encryptionService.isAvailable()) {
            return expandKeyFromProperties(key);
        }

        EncryptedProperty property = encryptionService.get(key, EncryptedProperty.class);
        if (property == null) {
            return expandKeyFromProperties(key);
        }

        if (property.getEnvironment() != null && environment != null) {
            if (!Objects.equals(property.getEnvironment(), environment.getId())) {
                return expandKeyFromProperties(key);
            }
        }

        value = property.getValue();

        if (value != null) {
            return value;
        }

        return expandKeyFromProperties(key);
    }

    private String expandKeyFromProperties(String key) {
        String value = environmentProperties.get(key);

        if (value == null) {
            throw new IllegalStateException("Couldn't translate key '" + key + "'");
        }

        return value;
    }
}
