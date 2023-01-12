package com.evolveum.midpoint.studio.axiom.query.psi;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.studio.axiom.query.AxiomQueryTokenTypes;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AQFilterName extends LeafPsiElement {

    public AQFilterName(@NotNull CharSequence text) {
        super(AxiomQueryTokenTypes.getRuleElementType(AxiomQueryParser.RULE_filterName), text);
    }
}
