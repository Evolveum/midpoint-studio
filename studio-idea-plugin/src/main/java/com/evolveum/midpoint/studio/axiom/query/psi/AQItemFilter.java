package com.evolveum.midpoint.studio.axiom.query.psi;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.studio.axiom.query.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQItemFilter extends CompositePsiElement {

    public AQItemFilter() {
        super(AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_filter));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
