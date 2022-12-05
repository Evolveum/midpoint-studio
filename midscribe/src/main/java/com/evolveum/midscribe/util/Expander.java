package com.evolveum.midscribe.util;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Expander {

    private static final Pattern PATTERN = Pattern.compile("\\$\\((\\S*?)\\)");

    private Properties properties;

    public Expander(Properties properties) {
        this.properties = properties;
    }

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
        Object value = properties.get(key);
        return value != null ? value.toString() : null;
    }
}
