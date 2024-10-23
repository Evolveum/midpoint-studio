package com.evolveum.midpoint.studio.lang.axiomquery.psi;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQFilterName extends CompositePsiElement {

    public AQFilterName() {
        super(AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_filterName));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
