package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.lang.Language;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryLanguage extends Language {

    public static final AxiomQueryLanguage INSTANCE = new AxiomQueryLanguage();

    public static final Icon ICON = MidPointIcons.Midpoint;

    private AxiomQueryLanguage() {
        super("Axiom Query");
    }
}
