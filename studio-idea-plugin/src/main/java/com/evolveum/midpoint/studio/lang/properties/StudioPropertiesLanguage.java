package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.lang.Language;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class StudioPropertiesLanguage extends Language {

    public static final StudioPropertiesLanguage INSTANCE = new StudioPropertiesLanguage();

    public static final Icon ICON = MidPointIcons.Midpoint;

    private StudioPropertiesLanguage() {
        super("Studio Properties");
    }
}
