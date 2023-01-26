package com.evolveum.midpoint.studio.lang.axiomquery.psi;

import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQPath extends CompositePsiElement {

    public AQPath() {
        super(AxiomQueryTokenTypes.getRuleElementType(com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryParserV2.RULE_path));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
