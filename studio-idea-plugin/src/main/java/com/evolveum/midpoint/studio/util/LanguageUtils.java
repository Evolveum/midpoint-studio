/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.util;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import org.jetbrains.yaml.YAMLLanguage;

public class LanguageUtils {

    public static Language findLanguageByID(String id) {
        return Language.getRegisteredLanguages().stream()
                .filter(l -> l.getID().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public static String getExtension(Language lang) {
        if (lang instanceof JsonLanguage) return "json";
        if (lang instanceof XMLLanguage) return "xml";
        if (lang instanceof YAMLLanguage) return "yaml";
        return "txt";
    }

    public static Language detectLanguage(String text) {
        String s = stripLeadingNoise(text);

        if (s.isEmpty()) return null;

        // XML
        if (s.startsWith("<")) {
            return XMLLanguage.INSTANCE;
        }

        // JSON
        if (s.startsWith("{") || s.startsWith("[")) {
            return JsonLanguage.INSTANCE;
        }

        // YAML document start or YAML mapping key
        if (s.startsWith("---") || s.matches("^[A-Za-z_][\\w-]*\\s*:.*")) {
            return YAMLLanguage.INSTANCE;
        }

        return PlainTextLanguage.INSTANCE;
    }

    public static String stripLeadingNoise(String text) {
        int i = 0;
        int n = text.length();

        while (i < n) {
            // whitespace
            while (i < n && Character.isWhitespace(text.charAt(i))) {
                i++;
            }

            // comments
            if (i < n && text.charAt(i) == '#') {
                while (i < n && text.charAt(i) != '\n') i++;
                continue;
            }

            if (i + 3 < n && text.startsWith("<!--", i)) {
                int end = text.indexOf("-->", i + 4);
                if (end == -1) return "";
                i = end + 3;
                continue;
            }

            if (i + 1 < n && text.startsWith("//", i)) {
                while (i < n && text.charAt(i) != '\n') i++;
                continue;
            }

            if (i + 1 < n && text.startsWith("/*", i)) {
                int end = text.indexOf("*/", i + 2);
                if (end == -1) return "";
                i = end + 2;
                continue;
            }

            break;
        }

        return text.substring(i);
    }
}
