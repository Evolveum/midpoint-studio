package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;

/**
 * Adapt ANTLR needs to intellij
 */
public class AxiomQueryLexerAdaptor extends ANTLRLexerAdaptor {

    private AxiomQueryLexerAdaptor(AxiomQueryLexer lexer) {
        super(AxiomQueryLanguage.INSTANCE, lexer);
    }

    public static AxiomQueryLexerAdaptor newInstance() {
        AxiomQueryLexer lexer = new AxiomQueryLexer(null);

        return new AxiomQueryLexerAdaptor(lexer);
    }
}
