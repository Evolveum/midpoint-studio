package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.midpoint.studio.lang.axiomquery.antlr.AxiomQueryLexerV2;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;

/**
 * Adapt ANTLR needs to intellij
 */
public class AxiomQueryLexerAdaptor extends ANTLRLexerAdaptor {

    private AxiomQueryLexerAdaptor(AxiomQueryLexerV2 lexer) {
        super(AxiomQueryLanguage.INSTANCE, lexer);
    }

    public static AxiomQueryLexerAdaptor newInstance() {
        AxiomQueryLexerV2 lexer = new AxiomQueryLexerV2(null);

        return new AxiomQueryLexerAdaptor(lexer);
    }
}
