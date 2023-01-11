package com.evolveum.midpoint.studio.axiom;

import com.evolveum.axiom.lang.antlr.AxiomLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;

/**
 * Adapt ANTLR needs to intellij
 */
public class AxiomLexerAdaptor extends ANTLRLexerAdaptor {

    public AxiomLexerAdaptor(AxiomLexer lexer) {
        super(AxiomLanguage.INSTANCE, lexer);
    }
}
