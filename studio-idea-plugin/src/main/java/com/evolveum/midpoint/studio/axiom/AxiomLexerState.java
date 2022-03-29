package com.evolveum.midpoint.studio.axiom;

import com.evolveum.axiom.lang.antlr.AxiomLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerState;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.misc.IntegerStack;
import org.antlr.v4.runtime.misc.MurmurHash;

public class AxiomLexerState extends ANTLRLexerState {
    /**
     * Tracks whether we are in a lexer rule, a parser rule or neither;
     * managed by the ANTLRv4Lexer grammar.
     */
    private final int currentRuleType;

    public AxiomLexerState(int mode, IntegerStack modeStack, int currentRuleType) {
        super(mode, modeStack);
        this.currentRuleType = currentRuleType;
    }

    public int getCurrentRuleType() {
        return currentRuleType;
    }

    @Override
    public void apply(Lexer lexer) {
        super.apply(lexer);

        if (lexer instanceof AxiomLexer) {
            ((AxiomLexer) lexer).setCurrentRuleType(getCurrentRuleType());
        }
    }

    @Override
    protected int hashCodeImpl() {
        int hash = MurmurHash.initialize();
        hash = MurmurHash.update(hash, getMode());
        hash = MurmurHash.update(hash, getModeStack());
        hash = MurmurHash.update(hash, getCurrentRuleType());
        return MurmurHash.finish(hash, 3);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AxiomLexerState)) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        AxiomLexerState other = (AxiomLexerState) obj;
        return this.getCurrentRuleType() == other.getCurrentRuleType();
    }
}
