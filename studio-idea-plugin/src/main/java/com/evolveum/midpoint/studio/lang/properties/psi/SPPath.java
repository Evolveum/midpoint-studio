package com.evolveum.midpoint.studio.lang.properties.psi;

import com.evolveum.midpoint.studio.lang.properties.StudioPropertiesTokenTypes;
import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPPath extends CompositePsiElement {

    public SPPath() {
        super(StudioPropertiesTokenTypes.RULE_ELEMENT_TYPES.get(StudioPropertiesParser.RULE_path));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }

    @Override
    public PsiReference getReference() {
        return new SPPathReference(this);
    }
}
