package com.evolveum.midpoint.studio.impl;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Expander {

    private static final Pattern PATTERN = Pattern.compile("\\$\\((\\S*?)\\)");

    private CredentialsManager credentialsManager;

    private EnvironmentProperties propertyManager;

    public Expander(CredentialsManager credentialsManager, @NotNull EnvironmentProperties propertyManager) {
        this.credentialsManager = credentialsManager;
        this.propertyManager = propertyManager;
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
        if (credentialsManager != null && credentialsManager.isAvailable()) {
            Credentials credentials = credentialsManager.get(key);
            if (credentials != null) {
                value = credentials.getPassword();
            }
        }

        if (value != null) {
            return value;
        }

        return propertyManager.get(key);
    }
}
