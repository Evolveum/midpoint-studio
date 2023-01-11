package com.evolveum.midpoint.studio.axiom.query;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;

/**
 * Adapt ANTLR needs to intellij
 */
public class AxiomQueryLexerAdaptor extends ANTLRLexerAdaptor {

    public AxiomQueryLexerAdaptor(AxiomQueryLexer lexer) {
        super(AxiomQueryLanguage.INSTANCE, lexer);
    }
}
