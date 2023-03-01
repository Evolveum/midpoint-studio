package com.evolveum.midpoint.studio.lang.properties.psi;

import com.evolveum.midpoint.studio.lang.properties.SPropertiesTokenTypes;
import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesPath extends CompositePsiElement {

    public SPropertiesPath() {
        super(SPropertiesTokenTypes.RULE_ELEMENT_TYPES.get(StudioPropertiesParser.RULE_path));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }

    @Override
    public PsiReference getReference() {
        return new SPropertiesPathReference(this);
    }
}
