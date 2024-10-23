package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.lang.Language;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesLanguage extends Language {

    public static final SPropertiesLanguage INSTANCE = new SPropertiesLanguage();

    public static final Icon ICON = MidPointIcons.Midpoint;

    private SPropertiesLanguage() {
        super("Studio Properties");
    }
}
