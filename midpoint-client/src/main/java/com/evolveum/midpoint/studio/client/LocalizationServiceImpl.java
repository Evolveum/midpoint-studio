package com.evolveum.midpoint.studio.client;

import com.evolveum.midpoint.common.LocalizationService;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.util.exception.CommonException;

import java.util.Locale;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LocalizationServiceImpl implements LocalizationService {

    @Override
    public String translate(LocalizableMessage msg, Locale locale, String defaultMessage) {
        String translated = translate(msg, locale);
        return translated != null ? translated : defaultMessage;
    }

    @Override
    public String translate(PolyString polyString, Locale locale, boolean allowOrig) {
        String def = allowOrig ? polyString.getOrig() : null;
        return translate(polyString.getOrig(), new Object[]{}, locale, def);
    }

    @Override
    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    @Override
    public String translate(String key, Object[] params, Locale locale) {
        return translate(key, params, locale, null);
    }

    @Override
    public String translate(String key, Object[] params, Locale locale, String defaultMessage) {
        if (defaultMessage != null) {
            return defaultMessage;
        }

        return key;
    }

    @Override
    public String translate(LocalizableMessage msg, Locale locale) {
        return msg != null ? msg.getFallbackMessage() : null;
    }

    @Override
    public <T extends CommonException> T translate(T e) {
        return e;
    }
}
