package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.lang.Language;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MelLanguage extends Language {

    public static final MelLanguage INSTANCE = new MelLanguage();

    private MelLanguage() {
        super(MelConstants.LANGUAGE);
    }
}
