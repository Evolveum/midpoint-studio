package com.evolveum.midpoint.studio.lang.axiom;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.lang.Language;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomLanguage extends Language {

    public static final AxiomLanguage INSTANCE = new AxiomLanguage();

    public static final Icon ICON = MidPointIcons.Midpoint;

    private AxiomLanguage() {
        super("Axiom");
    }
}
