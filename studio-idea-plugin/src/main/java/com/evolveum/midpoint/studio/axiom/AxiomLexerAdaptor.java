package com.evolveum.midpoint.studio.axiom;

import com.evolveum.axiom.lang.antlr.AxiomLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.v4.runtime.Lexer;

/**
 * Adapt ANTLR needs to intellij
 */
public class AxiomLexerAdaptor extends ANTLRLexerAdaptor {

    private static final AxiomLexerState INITIAL_STATE = new AxiomLexerState(Lexer.DEFAULT_MODE, null, 0);

    public AxiomLexerAdaptor(AxiomLexer lexer) {
        super(AxiomLanguage.INSTANCE, lexer);
    }

    @Override
    protected AxiomLexerState getInitialState() {
        return INITIAL_STATE;
    }

    @Override
    protected AxiomLexerState getLexerState(Lexer lexer) {
        if (lexer._modeStack.isEmpty()) {
            return new AxiomLexerState(lexer._mode, null, ((AxiomLexer) lexer).getCurrentRuleType());
        }

        return new AxiomLexerState(lexer._mode, lexer._modeStack, ((AxiomLexer) lexer).getCurrentRuleType());
    }
}
