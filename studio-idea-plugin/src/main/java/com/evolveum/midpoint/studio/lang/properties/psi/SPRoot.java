package com.evolveum.midpoint.studio.lang.properties.psi;

import com.evolveum.midpoint.studio.lang.properties.StudioPropertiesTokenTypes;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPRoot extends CompositePsiElement {

    public SPRoot() {
        super(StudioPropertiesTokenTypes.RULE_ELEMENT_TYPES.get(com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser.RULE_root));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
