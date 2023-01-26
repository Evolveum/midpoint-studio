package com.evolveum.midpoint.studio.lang.axiomquery.psi;

import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQFilter extends CompositePsiElement {

    public AQFilter() {
        super(AxiomQueryTokenTypes.getRuleElementType(com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryParserV2.RULE_filter));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
