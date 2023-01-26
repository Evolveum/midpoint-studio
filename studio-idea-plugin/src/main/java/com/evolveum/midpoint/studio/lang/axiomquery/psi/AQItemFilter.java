package com.evolveum.midpoint.studio.lang.axiomquery.psi;

import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryTokenTypes;
import com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryParserV2;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQItemFilter extends CompositePsiElement {

    public AQItemFilter() {
        super(AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParserV2.RULE_itemFilter));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
