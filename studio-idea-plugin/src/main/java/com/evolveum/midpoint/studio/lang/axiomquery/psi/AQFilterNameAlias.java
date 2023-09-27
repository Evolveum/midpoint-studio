package com.evolveum.midpoint.studio.lang.axiomquery.psi;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQFilterNameAlias extends LeafPsiElement {

    public AQFilterNameAlias(@NotNull CharSequence text) {
        super(AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_filterNameAlias), text);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
