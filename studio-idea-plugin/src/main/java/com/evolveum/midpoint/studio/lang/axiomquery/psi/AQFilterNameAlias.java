package com.evolveum.midpoint.studio.lang.axiomquery.psi;

import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryTokenTypes;
import com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryParserV2;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQFilterNameAlias extends LeafPsiElement {

    public AQFilterNameAlias(@NotNull CharSequence text) {
        super(AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParserV2.RULE_filterNameAlias), text);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
