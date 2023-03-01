package com.evolveum.midpoint.studio.lang.properties.psi;

import com.evolveum.midpoint.studio.lang.properties.SPropertiesTokenTypes;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesRoot extends CompositePsiElement {

    public SPropertiesRoot() {
        super(SPropertiesTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser.RULE_root));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
