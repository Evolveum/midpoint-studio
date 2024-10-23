package com.evolveum.midpoint.studio.lang.properties.psi;

import com.evolveum.midpoint.studio.lang.properties.SPropertiesTokenTypes;
import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesProperty extends CompositePsiElement {

    public SPropertiesProperty() {
        super(SPropertiesTokenTypes.RULE_ELEMENT_TYPES.get(StudioPropertiesParser.RULE_property));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
