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
        text = text.trim();

        if (text.startsWith("{") || text.startsWith("[")) return JsonLanguage.INSTANCE;
        if (text.startsWith("<")) return XMLLanguage.INSTANCE;
        if (text.contains(":") && !text.contains("{") && !text.contains("<")) return YAMLLanguage.INSTANCE;

        return PlainTextLanguage.INSTANCE;
    }
}
