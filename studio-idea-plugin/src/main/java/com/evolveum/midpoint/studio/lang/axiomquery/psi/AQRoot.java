package com.evolveum.midpoint.studio.lang.axiomquery.psi;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQRoot extends CompositePsiElement {

    public AQRoot() {
        super(AxiomQueryTokenTypes.RULE_ELEMENT_TYPES.get(AxiomQueryParser.RULE_root));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
