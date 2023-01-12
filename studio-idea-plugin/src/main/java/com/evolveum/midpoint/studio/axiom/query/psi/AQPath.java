package com.evolveum.midpoint.studio.axiom.query.psi;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.studio.axiom.query.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.CompositePsiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQPath extends CompositePsiElement {

    public AQPath() {
        super(AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_path));
    }
}
