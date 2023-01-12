package com.evolveum.midpoint.studio.axiom.query.psi;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.studio.axiom.query.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQFilterAlias extends LeafPsiElement {

    public AQFilterAlias(@NotNull CharSequence text) {
        super(AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_filterNameAlias), text);
    }
}
