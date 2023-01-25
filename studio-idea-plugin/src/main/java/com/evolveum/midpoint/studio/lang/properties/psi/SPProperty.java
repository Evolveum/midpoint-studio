package com.evolveum.midpoint.studio.lang.properties.psi;

import com.evolveum.midpoint.studio.lang.properties.StudioPropertiesTokenTypes;
import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPProperty extends CompositePsiElement {

    public SPProperty() {
        super(StudioPropertiesTokenTypes.RULE_ELEMENT_TYPES.get(StudioPropertiesParser.RULE_property));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
